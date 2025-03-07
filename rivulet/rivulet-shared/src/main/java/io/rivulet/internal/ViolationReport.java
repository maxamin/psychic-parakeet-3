package io.rivulet.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import io.rivulet.ExpectsRivuletRerun;
import io.rivulet.RerunResult;
import io.rivulet.ViolationReportingUtils;
import io.rivulet.internal.rerun.TestRerunConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

/* Record type that holds information about test runs, violations, test reruns and critical violations. */
public class ViolationReport {

    // Whether skipped test reruns should be recorded
    public static final boolean RECORD_SKIPPED_RERUNS = false;
    // String used to represent various parts of a violation that is a dummy violation made for reporting reruns
    private static final String DUMMY_VIOLATION_VALUE = "UNKNOWN-GENERATED-FROM_RERUN";
    // Maps test class names to a map from test method names to their ExpectsRivuletRerun annotation
    private static final HashMap<String, HashMap<String, ExpectsRivuletRerun>> annotationMap = new HashMap<>();
    // Set of TestRerunConfigurations for reruns that were skipped
    private static final HashSet<TestRerunConfiguration> skippedRerunConfigurations = new HashSet<>();

    // Maps base sinks names to the number of violations and critical violations reported at that sink
    private final LinkedHashMap<String, ViolationCount> violationsPerSink = new LinkedHashMap<>();
    // Map rerun generator classes to information about the reruns generated by that class
    private final LinkedHashMap<String, RerunCount> rerunsPerGenerator = new LinkedHashMap<>();
    // Maps class names to a map from test methods to violations
    private final LinkedHashMap<String, LinkedHashMap<String, TestInfo>> testsRun = new LinkedHashMap<>();

    public ViolationReport() {
        violationsPerSink.put("total", new ViolationCount());
        rerunsPerGenerator.put("total", new RerunCount());
    }

    public void writeJsonToFile(File reportFile) {
        addSkippedRerunInfo();
        addAnnotationInfo();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
        String json = gson.toJson(this);
        try {
            PrintWriter out = new PrintWriter(reportFile);
            out.println(json);
            out.close();
        } catch(FileNotFoundException e) {
            System.out.println("Failed to write phosphor report to: " + reportFile);
            e.printStackTrace();
        }
    }

    /* Fills in information about how many of the violations for a particular sink were verified by having at least one rerun
     * that resulted in a critical violation. */
    public void addVerifiedViolationsInformation() {
        for(String className : testsRun.keySet()) {
            for(String methodName : testsRun.get(className).keySet()) {
                for(Violation v : testsRun.get(className).get(methodName).violations) {
                    if(v.numberOfCriticalViolations() > 0) {
                        violationsPerSink.get(v.baseSink).verifiedViolations++;
                        violationsPerSink.get("total").verifiedViolations++;
                    }
                }
            }
        }
    }

    /* Fills information from the ExpectsRivuletReruns in annotationsMap into the TestInfo instances in testsRun. */
    private void addAnnotationInfo() {
        for(String className : testsRun.keySet()) {
            if(annotationMap.containsKey(className)) {
                for(String methodName : testsRun.get(className).keySet()) {
                    if(annotationMap.get(className).containsKey(methodName)) {
                        TestInfo info = testsRun.get(className).get(methodName);
                        ExpectsRivuletRerun annotation = annotationMap.get(className).get(methodName);
                        if(annotation != null) {
                            info.expectedReruns = annotation.numReruns();
                            info.expectedCriticalViolations = annotation.numCriticalViolations();
                        }
                    }
                }
            }
        }
    }

    /* Adds information about test reruns that were skipped if RECORD_SKIPPED_RERUNS is true. */
    private void addSkippedRerunInfo() {
        if(RECORD_SKIPPED_RERUNS) {
            for(TestRerunConfiguration rerunConfiguration : skippedRerunConfigurations) {
                reportTestRerunResult(rerunConfiguration.getTestClass(), rerunConfiguration.getTestMethod(), RerunResult.SKIPPED.message, RerunResult.SKIPPED.message, rerunConfiguration);
            }
            skippedRerunConfigurations.clear();
        }
    }

    /* Checks that the number of reruns and critical violations for each test run matches with the expected value. Returns
     * whether a check failed. */
    public boolean checkExpectedInfo() {
        boolean failed = false;
        for(String className : testsRun.keySet()) {
            for(String methodName : testsRun.get(className).keySet()) {
                TestInfo info = testsRun.get(className).get(methodName);
                if(hasRivuletExpectations(info)) {
                    String failureMessage = null;
                    if(info.expectedReruns != null) {
                        String type = (info.expectedReruns == 1 || info.expectedReruns == ExpectsRivuletRerun.AT_LEAST_ONE) ? "rerun" : "reruns";
                        failureMessage = checkRivuletExpected(info.expectedReruns, info.numberOfReruns(), type, failureMessage);
                    }
                    if(info.expectedCriticalViolations != null) {
                        String type = (info.expectedCriticalViolations == 1 || info.expectedCriticalViolations == ExpectsRivuletRerun.AT_LEAST_ONE) ? "critical violation" : "critical violations";
                        failureMessage = checkRivuletExpected(info.expectedCriticalViolations, info.numberOfCriticalViolations(), type, failureMessage);
                    }
                    String testName = ViolationReportingUtils.formatTestName(className, methodName);
                    if(failureMessage == null) {
                        String result = ViolationReportingUtils.colorText("[RERUN-CRITERIA-SUCCESS]", ViolationReportingUtils.RivuletColor.SUCCESS);
                        result = ViolationReportingUtils.boldText(result);
                        System.out.printf("%s %s\n", result, testName);
                    } else {
                        failed = true;
                        String result = ViolationReportingUtils.colorText("[RERUN-CRITERIA-FAILURE]", ViolationReportingUtils.RivuletColor.FAILURE);
                        result = ViolationReportingUtils.boldText(result);
                        System.out.printf("%s %s - %s\n", result, testName, failureMessage);
                    }
                }
            }
        }
        return failed;
    }

    /* Returns whether the specified TestInfo's reruns or critical violations are expected to meet some criteria. */
    private boolean hasRivuletExpectations(TestInfo info) {
        return (info.expectedCriticalViolations != null && info.expectedCriticalViolations != ExpectsRivuletRerun.ANY) ||
                (info.expectedReruns != null && info.expectedReruns != ExpectsRivuletRerun.ANY);
    }

    /* Checks that the specified actual value meets the specified expected criteria. Adds any failures to the end of the specified
     * message and return the new message. */
    private String checkRivuletExpected(int expected, int actual, String expectedType, String message) {
        String failure = null;
        if(expected == ExpectsRivuletRerun.AT_LEAST_ONE) {
            if(actual < 1) {
                failure = String.format("Expected at least one %s but got %d.", expectedType, actual);
            }
        } else if(expected != ExpectsRivuletRerun.ANY && actual != expected) {
            failure = String.format("Expected %d %s but got %d.", expected, expectedType, actual);
        }
        if(message == null) {
            return failure;
        } else if(failure == null) {
            return message;
        } else {
            return message + " " + failure;
        }
    }

    /* Clears the expected reruns and critical violations information from each test run. */
    public void clearExpectedInfo() {
        for(LinkedHashMap<String, TestInfo> methodMap : testsRun.values()) {
            for(TestInfo info : methodMap.values()) {
                info.expectedCriticalViolations = null;
                info.expectedReruns = null;
            }
        }
    }

    /* Adds information to indicate that the specified violation was reported. */
    public synchronized void reportViolation(io.rivulet.internal.Violation violation) {
        String className = violation.getTestClass();
        String methodName = violation.getTestMethod();
        reportTestWasRun(className, methodName);
        violationsPerSink.putIfAbsent(violation.getBaseSink(), new ViolationCount());
        ViolationCount count = violationsPerSink.get(violation.getBaseSink());
        ViolationCount total = violationsPerSink.get("total");
        testsRun.get(className).get(methodName).violations.add(new Violation(violation));
        count.violations++;
        total.violations++;

    }

    /* Adds information to indicate that a test was run. */
    public synchronized void reportTestWasRun(String className, String methodName) {
        // Add information that indicates that a test was run
        if(!testsRun.containsKey(className)) {
            testsRun.put(className, new LinkedHashMap<>());
        }
        LinkedHashMap<String, TestInfo> methodMap = testsRun.get(className);
        if(!methodMap.containsKey(methodName)) {
            methodMap.put(methodName, new TestInfo());
        }
    }

    /* Adds information about the result of a test rerun. */
    public synchronized void reportTestRerunResult(String className, String methodName, String criticalViolationStatus, String testOutcome, TestRerunConfiguration currentConfig) {
        reportTestWasRun(className, methodName);
        // Create a new rerun from the current configuration's information
        Rerun rerunResult = new Rerun(currentConfig.getReplacementRepresentations(), criticalViolationStatus, testOutcome);
        TestInfo testInfo = testsRun.get(className).get(methodName);
        // Add the new rerun's info to all violations that match one of uniqueIDs of the current configuration, if
        // no violations match a uniqueID create a dummy node to represent the original violation
        for(String uniqueID : currentConfig.getViolationUIDs()) {
            boolean foundMatch = false;
            for(Violation v : testInfo.violations) {
                if(v.uniqueID.equals(uniqueID)) {
                    foundMatch = true;
                    v.reruns.add(rerunResult);
                    break;
                }
            }
            // If no violations match the uniqueID add a new dummy violation
            if(!foundMatch) {
                Violation dummy = new Violation(uniqueID);
                dummy.reruns.add(rerunResult);
                testInfo.violations.add(dummy);
            }
        }
        // Update the RerunCount
        String generator = currentConfig.getAutoTainterClass().getSimpleName();
        rerunsPerGenerator.putIfAbsent(generator, new RerunCount());
        RerunCount count = rerunsPerGenerator.get(generator);
        RerunCount total = rerunsPerGenerator.get("total");
        count.rerunsExecuted++;
        total.rerunsExecuted++;
        if(RerunResult.CRITICAL_VIOLATION.message.equals(criticalViolationStatus)) {
            count.criticalReruns++;
            total.criticalReruns++;
        }
    }

    /* Adds information from the specified other report to this report. */
    public void merge(ViolationReport other) {
        // Merge violation per sink information
        for(String sink : other.violationsPerSink.keySet()) {
            if(!violationsPerSink.containsKey(sink)) {
                violationsPerSink.put(sink, other.violationsPerSink.get(sink));
            } else {
                violationsPerSink.get(sink).sum(other.violationsPerSink.get(sink));
            }
        }
        for(String generator : other.rerunsPerGenerator.keySet()) {
            if(!rerunsPerGenerator.containsKey(generator)) {
                rerunsPerGenerator.put(generator, other.rerunsPerGenerator.get(generator));
            } else {
                rerunsPerGenerator.get(generator).sum(other.rerunsPerGenerator.get(generator));
            }
        }
        // Merge tests run information
        for(String className : other.testsRun.keySet()) {
            for(String methodName : other.testsRun.get(className).keySet()) {
                reportTestWasRun(className, methodName);
                TestInfo thisInfo = testsRun.get(className).get(methodName);
                TestInfo otherInfo = other.testsRun.get(className).get(methodName);
                thisInfo.expectedReruns = (thisInfo.expectedReruns == ExpectsRivuletRerun.ANY) ? otherInfo.expectedReruns : thisInfo.expectedReruns;
                thisInfo.expectedCriticalViolations = (thisInfo.expectedCriticalViolations == ExpectsRivuletRerun.ANY) ? otherInfo.expectedCriticalViolations : thisInfo.expectedCriticalViolations;
                for(Violation otherViolation : otherInfo.violations) {
                    Iterator<Violation> it = thisInfo.violations.iterator();
                    boolean replace = false;
                    boolean found = false;
                    while(it.hasNext()) {
                        Violation thisViolation = it.next();
                        if(thisViolation.uniqueID.equals(otherViolation.uniqueID)) {
                            found = true;
                            if(thisViolation.baseSink.equals(DUMMY_VIOLATION_VALUE)) {
                                otherViolation.reruns.addAll(thisViolation.reruns);
                                replace = true;
                                it.remove();
                            } else {
                                thisViolation.reruns.addAll(otherViolation.reruns);
                            }
                        }
                    }
                    if(replace || !found) {
                        thisInfo.violations.add(otherViolation);
                    }
                }
            }
        }
    }

    public static ViolationReport readJsonFromFile(File reportFile) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
            JsonReader reader = new JsonReader(new FileReader(reportFile));
            reader.setLenient(true);
            ViolationReport report = gson.fromJson(reader, ViolationReport.class);
            reader.close();
            return report;
        } catch(Exception e) {
            return new ViolationReport();
        }
    }

    /* Stores information about the ExpectsRivuletRerun annotation for a test. */
    public static synchronized void reportAnnotation(String className, String methodName, ExpectsRivuletRerun annotation) {
        annotationMap.putIfAbsent(className, new HashMap<>());
        HashMap<String, ExpectsRivuletRerun> methodAnnotationMap = annotationMap.get(className);
        methodAnnotationMap.putIfAbsent(methodName, annotation);
    }

    /* Stores information to indicate that the rerun for the specified configuration was skipped. */
    public static synchronized void reportSkippedRerun(TestRerunConfiguration rerunConfiguration) {
        skippedRerunConfigurations.add(rerunConfiguration);
    }

    private static class TestInfo {
        final LinkedList<Violation> violations = new LinkedList<>();
        Integer expectedReruns = ExpectsRivuletRerun.ANY;
        Integer expectedCriticalViolations = ExpectsRivuletRerun.ANY;

        int numberOfReruns() {
            int count = 0;
            for(Violation v : violations) {
                count += v.reruns.size();
            }
            return count;
        }

        int numberOfCriticalViolations() {
            int count = 0;
            for(Violation v : violations) {
                count += v.numberOfCriticalViolations();
            }
            return count;
        }
    }

    private static class Violation {
        final String baseSink;
        final String actualSinkClass;
        final String uniqueID;
        final TaintedValue[] taintedValues;
        final HashSet<Rerun> reruns;

        /* Creates a placeholder dummy violation for an original violation with the specified uniqueID. */
        Violation(String uniqueID) {
            this.baseSink = DUMMY_VIOLATION_VALUE;
            this.actualSinkClass = DUMMY_VIOLATION_VALUE;
            this.uniqueID = uniqueID;
            this.taintedValues = new TaintedValue[0];
            this.reruns = new HashSet<>();
        }

        Violation(io.rivulet.internal.Violation violation) {
            this.baseSink = violation.getBaseSink();
            this.actualSinkClass = violation.getActualSinkClass();
            this.uniqueID = violation.getUniqueID();
            this.taintedValues = new TaintedValue[violation.getTaintedValues().size()];
            int i = 0;
            for(TaintedSinkValue val : violation.getTaintedValues()) {
                taintedValues[i++] = new TaintedValue(val);
            }
            this.reruns = new HashSet<>();
        }

        /* Returns the number of reruns for this violation that resulted in a critical violation. */
        int numberOfCriticalViolations() {
            int count = 0;
            for(Rerun rerun : reruns) {
                if(rerun.criticalViolationStatus.equals(RerunResult.CRITICAL_VIOLATION.message)) {
                    count++;
                }
            }
            return count;
        }
    }

    /* Stores information about a tainted value that reached a sink. */
    private static class TaintedValue {
        String[] sinkValues;
        String sinkValueClass;
        int sinkArgIndex;
        TaintSource[] taintSources;

        TaintedValue(TaintedSinkValue val) {
            this.sinkValues = val.getFormattedSinkValues().toArray(new String[0]);
            this.sinkValueClass = val.getSinkValueClass().toString();
            this.sinkArgIndex = val.getSinkArgIndex();
            this.taintSources = new TaintSource[val.getTaintSources().size()];
            int i = 0;
            for(SourceInfoTaintLabel label : val.getTaintSources()) {
                taintSources[i++] = new TaintSource(label);
            }
        }
    }

    /* Stores information about the source of some tainted values label. */
    private static class TaintSource {
        String baseSource;
        String actualSourceClass;
        int sourceArgIndex;
        String sourceValueClass;

        TaintSource(SourceInfoTaintLabel label) {
            this.baseSource = label.getBaseSource();
            this.actualSourceClass = label.getActualSourceClass();
            this.sourceArgIndex = label.getSourceArgIndex();
            this.sourceValueClass = label.getSourceValueClass().toString();
        }
    }

    /* Stores information about a test rerun. */
    private static class Rerun {
        final String[] replacements;
        final String criticalViolationStatus;
        final String testOutcome;

        Rerun(String[] replacements, String criticalViolationStatus, String testOutcome) {
            this.replacements = replacements;
            this.criticalViolationStatus = criticalViolationStatus;
            this.testOutcome = testOutcome;
        }
    }

    /* Stores information about the number of critical and non-critical violations. */
    private static class ViolationCount {
        int violations = 0;
        int verifiedViolations = 0;

        /* Adds the specified ViolationCount's counts to this ViolationCount's counts. */
        void sum(ViolationCount other) {
            this.violations += other.violations;
        }
    }

    private static class RerunCount {
        int rerunsExecuted = 0;
        int criticalReruns = 0;

        void sum(RerunCount other) {
            this.rerunsExecuted += other.rerunsExecuted;
            this.criticalReruns += other.criticalReruns;
        }
    }
}
