/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.java.monitor.ClassVisitor;
import org.eclipse.steady.shared.connectivity.Service;
import org.eclipse.steady.shared.util.FileSearch;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Analyzes a single Spring Boot application (JAR) as to identify (and
 * potentially instrument) all its classes (in directory BOOT-INF/classes), as
 * well as its JARs (in directory BOOT-INF/lib).
 */
@NotThreadSafe
public class SpringBootAnalyzer extends JarAnalyzer {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(SpringBootAnalyzer.class);

  private static final String INCL_SPACE = "vulas.core.instr.static.inclSpace";
  private static final String INCL_BACKEND_URL = "vulas.core.instr.static.inclBackendUrl";

  //	private static final ClassPool CLASSPOOL = ClassPool.getDefault();
  //	/**
  //	 * Adds a given URL to the classpath of the class pool. This allows maintaining dependencies
  // needed for the compilation of instrumented classes.
  //	 * @param _url
  //	 * @throws NotFoundException
  //	 */
  //	public static void insertClasspath(String _url) throws NotFoundException {
  // CLASSPOOL.insertClassPath(_url); }
  //	private static ClassPool getClassPool() { return WarAnalyzer.CLASSPOOL; }

  //	private static boolean OVERWRITE_ENABLED = false;
  //	public static void enableOverwrite(boolean _b) { WarAnalyzer.OVERWRITE_ENABLED = _b; }
  //	public static boolean isOverwriteEnabled() { return WarAnalyzer.OVERWRITE_ENABLED; }

  private Map<JavaId, ClassVisitor> instrumentedClasses = new HashMap<JavaId, ClassVisitor>();
  private Path tmpDir = null; // To where the JAR is extracted
  private Path inclDir = null;

  private ArchiveAnalysisManager mgr = null;
  private Set<FileAnalyzer> nestedAnalyzers = null;

  /** The Steady archive in directory target/vulas/incl/ will be rewritten as to
   * contain additional, app-specific configuration settings in the embedded
   * configuration file steady-core.properties. The original will then need to be skipped, i.e., not
   * included in the instrumented JAR, see {@link SpringBootAnalyzer#setIncludeDir(Path)}. */
  private Set<Path> ignoredIncludes = new HashSet<Path>();

  /** {@inheritDoc} */
  @Override
  public String[] getSupportedFileExtensions() {
    return new String[] {"jar"};
  }

  /**
   * Returns true if the archive has file extension 'jar' and contains
   * a {@link JarEntry} 'BOOT-INF/', false otherwise.
   */
  @Override
  public boolean canAnalyze(File _file) {
    final String ext = FileUtil.getFileExtension(_file);
    if (ext == null || ext.equals("")) return false;
    for (String supported_ext : this.getSupportedFileExtensions()) {
      if (supported_ext.equalsIgnoreCase(ext)) {
        try {
          final JarWriter jw = new JarWriter(_file.toPath());
          if (jw.hasEntry("BOOT-INF/")) {
            return true;
          } else {
            return false;
          }
        } catch (IOException ioe) {
          log.error(ioe.getMessage());
        }
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void analyze(final File _file) throws FileAnalysisException {
    try {
      super.analyze(_file);

      // Extract the JAR
      if (this.workDir != null)
        this.tmpDir = Paths.get(this.workDir.toString(), "spring_boot_analysis");
      else {
        try {
          this.tmpDir = java.nio.file.Files.createTempDirectory("spring_boot_analysis_");
        } catch (IOException e) {
          throw new IllegalStateException("Unable to create temp directory", e);
        }
      }
      this.jarWriter.extract(this.tmpDir);

      // Add BOOT-INF/classes to the classpath
      try {
        JarAnalyzer.insertClasspath(
            Paths.get(this.tmpDir.toString(), "BOOT-INF", "classes").toString());
      } catch (Exception e) {
        // No problem at all if instrumentation is not requested.
        // If instrumentation is requested, however, some classes may not compile
        SpringBootAnalyzer.log.error(
            this.toString() + ": Error while updating the classpath: " + e.getMessage());
      }

      // Add BOOT-INF/lib to the classpath
      try {
        final FileSearch lib_search = new FileSearch(new String[] {"jar"});
        final Set<Path> libs =
            lib_search.search(Paths.get(this.tmpDir.toString(), "BOOT-INF", "lib"));
        for (Path lib : libs) {
          JarAnalyzer.insertClasspath(lib.toString());
        }
      } catch (Exception e) {
        // No problem at all if instrumentation is not requested.
        // If instrumentation is requested, however, some classes may not compile
        SpringBootAnalyzer.log.error(
            this.toString() + ": Error while updating the classpath: " + e.getMessage());
      }
    } catch (IllegalStateException e) {
      log.error("IllegalStateException when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    } catch (IOException e) {
      log.error("IOException when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Exception when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    }
  }

  /**
   * Sets the directory containing JARs to be included in the rewritten JAR. All
   * JARs contained therein will be modified as well in order to set the
   * application scope in the configuration file steady-core.properties.
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  public void setIncludeDir(Path _p) {
    this.inclDir = _p;

    // If the WAR is to be instrumented, rewrite all JARs in the inclDir in order to include the
    // current artifact coordinates in the Vulas configuration file
    if (this.inclDir != null && this.instrument) {
      final FileSearch vis = new FileSearch(new String[] {"jar"});
      final Set<Path> libs = vis.search(this.inclDir);
      JarWriter writer = null;
      for (Path lib : libs) {
        try {
          writer = new JarWriter(lib);
          if (writer.hasEntry("steady-core.properties") && !writer.isRewrittenByVulas()) {

            // Rewrite the configuration file
            writer.setClassifier("and-config");
            writer.register("steady-core.properties", this);
            writer.rewrite(this.inclDir);

            // If we reach this point, the rewriting worked and the original file can be ignored
            // later on
            this.ignoredIncludes.add(lib);
            // Files.move(..., lib, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          }
        } catch (IOException ioe) {
          SpringBootAnalyzer.log.error("Error when rewriting the JARs: " + ioe.getMessage());
        } catch (JarAnalysisException jae) {
          SpringBootAnalyzer.log.error("Error when rewriting the JARs: " + jae.getMessage());
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * Determines whether the instrumented JAR is renamed or not. If yes, the new file name follows the following format:
   * - If app context is provided: [originalJarName]-vulas-[appGroupId]-[appArtifactId]-[appVersion].jar
   * - Otherwise: [originalJarName]-vulas.jar
   */
  public void setRename(boolean _b) {
    this.rename = _b;
  }

  /**
   * {@inheritDoc}
   *
   * See here: http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
   */
  @Override
  protected synchronized void createInstrumentedArchive() throws JarAnalysisException {

    // Additional manifest file entries
    this.jarWriter.addManifestEntry(
        "Steady-classInstrStats",
        "["
            + this.instrControl.countClassesTotal()
            + " total, "
            + this.instrControl.countClassesInstrumentedAlready()
            + " existed, "
            + this.instrControl.countClassesInstrumentedSuccess()
            + " ok, "
            + this.instrControl.countClassesInstrumentedFailure()
            + " err]");
    this.jarWriter.addManifestEntry(
        "Steady-constructStats", "[" + constructs.size() + " constructs]");
    if (JarAnalyzer.getAppContext() != null)
      this.jarWriter.addManifestEntry(
          "Steady-appContext",
          JarAnalyzer.getAppContext().getMvnGroup()
              + ":"
              + JarAnalyzer.getAppContext().getArtifact()
              + ":"
              + JarAnalyzer.getAppContext().getVersion());

    // Register this WarAnalyzer for callbacks
    this.jarWriter.register("^.*.class$", this);
    this.jarWriter.register("^BOOT-INF/classes/.*.class$", this);
    this.jarWriter.register("^BOOT-INF/lib/.*.jar$", this);

    // Additional files to be added to the JAR
    if (this.inclDir != null && this.inclDir.toFile().exists()) {

      // Include all JARs in inclDir in BOOT-INF/lib
      final FileSearch vis = new FileSearch(new String[] {"jar"});
      final Set<Path> libs = vis.search(this.inclDir);
      SpringBootAnalyzer.log.info(
          this
              + ": Dir ["
              + this.inclDir
              + "] includes another ["
              + libs.size()
              + "] JARs, to be included in BOOT-INF/lib");

      // Add all files in incl (except the one(s) rewritten to contain a proper
      // steady-cfg.properties)
      // this.jarWriter.addFiles("BOOT-INF/lib", libs, true);
      for (Path l : libs) {
        if (this.ignoredIncludes.contains(l)) continue;
        else this.jarWriter.addFile("BOOT-INF/lib", l, true);
      }

      // Add configuration files
      this.jarWriter.addFile(
          "BOOT-INF/classes/", Paths.get(this.inclDir.toString(), "steady-cfg.properties"), true);
      this.jarWriter.addFile(
          "BOOT-INF/classes/", Paths.get(this.inclDir.toString(), "vulas-cfg.xml"), true);
      this.jarWriter.addFile(
          "BOOT-INF/classes/", Paths.get(this.inclDir.toString(), "log4j.properties"), false);
    }

    // Rename
    if (this.rename) this.jarWriter.setClassifier("steady-instr");

    // Rewrite
    this.jarWriter.setCompressNewJarEntries(false);
    this.jarWriter.rewrite(this.workDir);

    // Stats
    this.instrControl.logStatistics();
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChilds() {
    return this.getChilds(true) != null && !this.getChilds(true).isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public Set<FileAnalyzer> getChilds(boolean _recursive) {
    if (this.mgr == null) {
      this.mgr = new ArchiveAnalysisManager(4, -1, this.instrument, JarAnalyzer.getAppContext());

      // Create a lib_mod folder for instrumented JARs of the WAR
      if (this.instrument) {
        final Path lib_mod = Paths.get(this.tmpDir.toString(), "BOOT-INF", "lib_mod");
        if (!lib_mod.toFile().exists()) {
          try {
            Files.createDirectory(lib_mod);
          } catch (IOException e) {
            log.error(
                "Cannot create directory [" + lib_mod.toAbsolutePath() + "]: " + e.getMessage(), e);
          }
        }

        if (lib_mod.toFile().exists()) {
          mgr.setWorkDir(lib_mod);
        } else {
          log.warn("Instrumentation disabled");
          mgr.setInstrument(false);
        }
      }

      // Find and analyze all JARs
      final Set<Path> jars =
          new FileSearch(new String[] {"jar"})
              .search(Paths.get(this.tmpDir.toString(), "BOOT-INF", "lib"));
      mgr.startAnalysis(jars, this);
      this.nestedAnalyzers = new HashSet<FileAnalyzer>();
      this.nestedAnalyzers.addAll(mgr.getAnalyzers());
    }
    return this.nestedAnalyzers;
  }

  /**
   * {@inheritDoc}
   *
   * Identifies all {@link ConstructId}s of all methods and constructors contained in the WAR file.
   * Returns true if the WAR has classes in BOOT-INF/classes, false otherwise. Note that classes in libraries contained in BOOT-INF/lib are ignored.
   */
  @Override
  public synchronized Set<ConstructId> getConstructIds() {
    if (this.constructs == null) {
      this.constructs = new TreeSet<ConstructId>();
      // Loop all *.class files in BOOT-INF/classes
      final Set<String> class_names = new HashSet<String>();
      String class_name = null;
      final Set<Path> class_files =
          new FileSearch(new String[] {"class"})
              .search(Paths.get(this.tmpDir.toString(), "BOOT-INF", "classes"));
      for (Path p : class_files) {
        class_name = p.toString();
        class_name = class_name.substring(0, class_name.length() - 6); // ".class"
        class_name =
            class_name.substring(class_name.indexOf("BOOT-INF") + 17); // "BOOT-INF/classes/"
        class_name = class_name.replace(File.separatorChar, '.');
        class_names.add(class_name);
        SpringBootAnalyzer.log.debug("Found [" + class_name + "]");
      }

      // Visit all classes using Javassist (and instrument as many as possible - if requested)
      CtClass ctclass = null;
      ClassVisitor cv = null;

      for (String cn : class_names) {
        try {
          ctclass = JarAnalyzer.getClassPool().get(cn);

          // Ignore interfaces (no executable code) and enums (rarely containing executable code,
          // perhaps to be included later on)
          if (ctclass.isInterface()) {
            this.interfaceCount++;
          } else {

            if (ctclass.isEnum()) this.enumCount++;
            else this.classCount++;

            // Create ClassVisitor for the current Java class
            cv = new ClassVisitor(ctclass);
            this.constructs.addAll(cv.getConstructs());

            // Instrument (if requested and not blacklisted)
            if (this.instrument && !this.instrControl.isBlacklistedClass(cn)) {
              // The SpringBoot JAR itself is not a dependency, hence, we do not set the digest
              // cv.setOriginalArchiveDigest(this.getSHA1());

              cv.setAppContext(SpringBootAnalyzer.getAppContext());
              if (cv.isInstrumented())
                this.instrControl.updateInstrumentationStatistics(cv.getJavaId(), null);
              else {
                try {
                  cv.visitMethods(true);
                  cv.visitConstructors(true);
                  cv.finalizeInstrumentation();
                  this.instrumentedClasses.put(cv.getJavaId(), cv);
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(true));
                } catch (IOException ioe) {
                  SpringBootAnalyzer.log.error(
                      "I/O exception while instrumenting class ["
                          + cv.getJavaId().getQualifiedName()
                          + "]: "
                          + ioe.getMessage());
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(false));
                } catch (CannotCompileException cce) {
                  SpringBootAnalyzer.log.warn(
                      "Cannot compile instrumented class ["
                          + cv.getJavaId().getQualifiedName()
                          + "]: "
                          + cce.getMessage());
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(false));
                } catch (Exception e) {
                  SpringBootAnalyzer.log.error(
                      e.getClass().getName()
                          + " occured while instrumenting class ["
                          + cv.getJavaId().getQualifiedName()
                          + "]: "
                          + e.getMessage());
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(false));
                }
              }
            }
          }
          if (!this.instrument) {
            // only detach if no static instrumentation (otherwise it will fail because the class
            // was modified)
            // in case the instrumentation is performed the detach is done in
            // ClassVisitor.finalizeInstrumentation
            ctclass.detach();
          }
        } catch (NotFoundException nfe) {
          SpringBootAnalyzer.log.error(
              "Error while analyzing class ["
                  + cv.getJavaId().getQualifiedName()
                  + "]: "
                  + nfe.getMessage());
          continue;
        } catch (RuntimeException re) {
          SpringBootAnalyzer.log.error(
              "Error while analyzing class [" + ctclass.getName() + "]: " + re.getMessage());
          continue;
        }
      }
      if (this.instrument)
        SpringBootAnalyzer.log.info(
            this.toString()
                + ": classes and enums comprised/already-instr/instr/not-instr ["
                + this.instrControl.countClassesTotal()
                + "/"
                + this.instrControl.countClassesInstrumentedAlready()
                + "/"
                + this.instrControl.countClassesInstrumentedSuccess()
                + "/"
                + this.instrControl.countClassesInstrumentedFailure()
                + "], constructs comprised ["
                + constructs.size()
                + "]");
      else
        SpringBootAnalyzer.log.info(
            this.toString()
                + ": constructs comprised ["
                + constructs.size()
                + "], classes ["
                + this.classCount
                + "], enums ["
                + enumCount
                + "], interfaces (ignored) ["
                + interfaceCount
                + "]");
    }
    return this.constructs;
  }

  /**
   * {@inheritDoc}
   *
   * In case the archive is rewritten, this method is used to rewrite certain {@link JarEntry}s
   * (rather than taking the file from the original archive).
   * The callback registration takes place in {@link #createInstrumentedArchive()}.
   */
  @Override
  public RewrittenJarEntry getInputStream(String _regex, JarEntry _entry) {
    InputStream is = null;
    long size = -1;
    long crc32 = -1;

    // Called during rewrite of classes
    if (_regex.equals("^.*.class$")) {
      JavaId jid = null;
      // Create JavaId from entry name
      try {
        String class_name = _entry.getName();
        class_name = class_name.substring(0, class_name.length() - 6); // ".class"
        // class_name = class_name.substring(16); // "BOOT-INF/classes/"
        class_name = class_name.replace('/', '.');
        jid = JavaId.parseClassQName(class_name);
      } catch (Exception e) {
        SpringBootAnalyzer.log.error(
            "Cannot parse Java Id from Jar Entry [" + _entry.getName() + "]: " + e.getMessage());
        jid = null;
      }

      // Create input stream
      if (jid != null && this.instrumentedClasses.get(jid) != null) {
        final byte[] bytecode = this.instrumentedClasses.get(jid).getBytecode();
        crc32 = FileUtil.getCRC32(bytecode);
        size = bytecode.length;
        is = new ByteArrayInputStream(bytecode);
        return new RewrittenJarEntry(is, size, crc32);
      }
    } else if (_regex.equals("^BOOT-INF/classes/.*.class$")) {
      JavaId jid = null;
      // Create JavaId from entry name
      try {
        String class_name = _entry.getName();
        class_name = class_name.substring(0, class_name.length() - 6); // ".class"
        class_name = class_name.substring(17); // "BOOT-INF/classes/"
        class_name = class_name.replace('/', '.');
        jid = JavaId.parseClassQName(class_name);
      } catch (Exception e) {
        SpringBootAnalyzer.log.error(
            "Cannot parse Java Id from Jar Entry [" + _entry.getName() + "]: " + e.getMessage());
        jid = null;
      }

      // Create input stream
      if (jid != null && this.instrumentedClasses.get(jid) != null) {
        final byte[] bytecode = this.instrumentedClasses.get(jid).getBytecode();
        crc32 = FileUtil.getCRC32(bytecode);
        size = bytecode.length;
        is = new ByteArrayInputStream(bytecode);
        return new RewrittenJarEntry(is, size, crc32);
      } else {
        SpringBootAnalyzer.log.error(
            "No instrumented class found for Jar Entry [" + _entry.getName() + "]");
      }
    }
    // Called during rewrite of JARs
    else if (_regex.equals("^BOOT-INF/lib/.*.jar$")) {
      JarAnalyzer ja = null;
      try {
        final String[] path_elements = _entry.getName().split("/");
        final Path p =
            Paths.get(
                path_elements[0],
                path_elements[1],
                path_elements[2]); // Assumming: BOOT-INF/lib/xyz.jar
        ja = mgr.getAnalyzerForSubpath(p);
        if (ja != null) {
          final File f = ja.getInstrumentedArchive();
          crc32 = FileUtil.getCRC32(f);
          size = f.length();
          is = new FileInputStream(f);
          return new RewrittenJarEntry(is, size, crc32);
        } else {
          SpringBootAnalyzer.log.warn("Cannot find JarAnalyzer for path [" + p + "]");
        }
      } catch (Exception e) {
        SpringBootAnalyzer.log.error(
            "Cannot find rewritten JAR file from [" + ja + "]: " + e.getMessage());
      }
    }
    // Called during the rewrite of the JARs in inclDir
    else if (_regex.equals("steady-core.properties")) {
      Path tmp_file = null;
      try {
        final PropertiesConfiguration cfg = new PropertiesConfiguration("steady-core.properties");
        cfg.setProperty(CoreConfiguration.APP_CTX_GROUP, JarAnalyzer.getAppContext().getMvnGroup());
        cfg.setProperty(CoreConfiguration.APP_CTX_ARTIF, JarAnalyzer.getAppContext().getArtifact());
        cfg.setProperty(CoreConfiguration.APP_CTX_VERSI, JarAnalyzer.getAppContext().getVersion());

        // Include space
        if (VulasConfiguration.getGlobal().getConfiguration().getBoolean(INCL_SPACE, true)
            && VulasConfiguration.getGlobal()
                .getConfiguration()
                .containsKey(CoreConfiguration.SPACE_TOKEN))
          cfg.setProperty(
              CoreConfiguration.SPACE_TOKEN,
              VulasConfiguration.getGlobal()
                  .getConfiguration()
                  .getString(CoreConfiguration.SPACE_TOKEN));

        // Include backend URL
        if (VulasConfiguration.getGlobal().getConfiguration().getBoolean(INCL_BACKEND_URL, true)
            && VulasConfiguration.getGlobal()
                .getConfiguration()
                .containsKey(VulasConfiguration.getServiceUrlKey(Service.BACKEND)))
          cfg.setProperty(
              VulasConfiguration.getServiceUrlKey(Service.BACKEND),
              VulasConfiguration.getGlobal()
                  .getConfiguration()
                  .getString(VulasConfiguration.getServiceUrlKey(Service.BACKEND)));

        tmp_file = Files.createTempFile("steady-core-", ".properties");
        cfg.save(tmp_file.toFile());

        crc32 = FileUtil.getCRC32(tmp_file.toFile());
        size = tmp_file.toFile().length();
        is = new FileInputStream(tmp_file.toFile());
        return new RewrittenJarEntry(is, size, crc32);
      } catch (ConfigurationException ce) {
        SpringBootAnalyzer.log.error(
            "Error when loading configuration from 'steady-core.properties': " + ce.getMessage());
      } catch (IOException ioe) {
        SpringBootAnalyzer.log.error(
            "Error when creating/reading temporary configuration file ["
                + tmp_file
                + "]: "
                + ioe.getMessage());
      }
    }

    return null;
  }
}
