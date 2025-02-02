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
package org.eclipse.steady.java.tasks;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.DirAnalyzer;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.FileAnalyzerFactory;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalConfigurationException;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.java.ArchiveAnalysisManager;
import org.eclipse.steady.java.JarAnalyzer;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.enums.Scope;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.util.DependencyUtil;
import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.ThreadUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.tasks.AbstractBomTask;

/**
 * <p>JavaBomTask class.</p>
 */
public class JavaBomTask extends AbstractBomTask {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(JavaBomTask.class);

  private static final String[] EXT_FILTER = new String[] {"jar", "war", "class", "java", "aar"};

  private String[] appPrefixes = null;

  private StringList appJarNames = null;

  private static final List<GoalClient> pluginGoalClients =
      Arrays.asList(GoalClient.MAVEN_PLUGIN, GoalClient.GRADLE_PLUGIN);

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getLanguage() {
    return new HashSet<ProgrammingLanguage>(
        Arrays.asList(new ProgrammingLanguage[] {ProgrammingLanguage.JAVA}));
  }

  /**
   * Returns true if the configuration setting {@link CoreConfiguration#APP_PREFIXES} shall be considered, false otherwise.
   */
  private final boolean useAppPrefixes() {
    return this.appPrefixes != null && !this.isOneOfGoalClients(pluginGoalClients);
  }

  /**
   * Returns true if the configuration setting {@link CoreConfiguration#APP_PREFIXES} shall be considered, false otherwise.
   */
  private final boolean useAppJarNames() {
    return this.appJarNames != null && !this.isOneOfGoalClients(pluginGoalClients);
  }

  /** {@inheritDoc} */
  @Override
  public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
    super.configure(_cfg);

    // App constructs identified using package prefixes
    this.appPrefixes = _cfg.getStringArray(CoreConfiguration.APP_PREFIXES, null);

    // Print warning message in case the setting is used as part of the Maven plugin
    if (this.appPrefixes != null && this.isOneOfGoalClients(pluginGoalClients)) {
      log.warn(
          "Configuration setting ["
              + CoreConfiguration.APP_PREFIXES
              + "] ignored when running the goal as Maven plugin");
      this.appPrefixes = null;
    }

    // App constructs identified using JAR file name patterns (regex)
    final String[] app_jar_names = _cfg.getStringArray(CoreConfiguration.APP_JAR_NAMES, null);
    if (app_jar_names != null) {
      // Print warning message in case the setting is used as part of the Maven plugin
      if (this.isOneOfGoalClients(pluginGoalClients)) {
        log.warn(
            "Configuration setting ["
                + CoreConfiguration.APP_JAR_NAMES
                + "] ignored when running the goal as Maven plugin");
        this.appJarNames = null;
      } else {
        this.appJarNames = new StringList();
        this.appJarNames.addAll(app_jar_names);
      }
    }

    // CLI: Only one of appPrefixes and appJarNames can be used
    if (!this.isOneOfGoalClients(pluginGoalClients)) {
      if (this.appPrefixes != null && this.appJarNames != null) {
        throw new GoalConfigurationException(
            "Exactly one of the configuration settings ["
                + CoreConfiguration.APP_PREFIXES
                + "] and ["
                + CoreConfiguration.APP_JAR_NAMES
                + "] must be set");
      } else if (this.appPrefixes == null && this.appJarNames == null) {
        throw new GoalConfigurationException(
            "Exactly one of the configuration settings ["
                + CoreConfiguration.APP_PREFIXES
                + "] and ["
                + CoreConfiguration.APP_JAR_NAMES
                + "] must be set");
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void execute() throws GoalExecutionException {

    // All app constructs
    final Set<ConstructId> app_constructs = new HashSet<ConstructId>();

    // Dependency files
    final Map<Path, JarAnalyzer> dep_files = new HashMap<Path, JarAnalyzer>();
    if (this.getKnownDependencies() != null) {
      for (Path p : this.getKnownDependencies().keySet()) dep_files.put(p, null);
    }

    // 1) Find app constructs by looping over all app paths
    if (this.hasSearchPath()) {
      for (Path p : this.getSearchPath()) {
        log.info(
            "Searching for Java constructs in search path ["
                + p
                + "] with filter ["
                + StringUtil.join(EXT_FILTER, ", ")
                + "] ...");
        final FileAnalyzer fa = FileAnalyzerFactory.buildFileAnalyzer(p.toFile(), EXT_FILTER);

        // Prefixes or jar name regex: Filter JAR constructs
        if (this.useAppPrefixes() || this.useAppJarNames()) {

          // All analyzers to loop over
          final Set<FileAnalyzer> analyzers = new HashSet<FileAnalyzer>();

          // Add child analyzers and analyzer itself (except DirAnalyzer)
          if (fa.hasChilds()) analyzers.addAll(fa.getChilds(true));
          if (!(fa instanceof DirAnalyzer)) analyzers.add(fa);

          // Log
          int count = 0;
          if (this.useAppPrefixes()) {
            log.info(
                "Looping over Java archive analyzers to separate application and dependency code"
                    + " using package prefix(es) ["
                    + StringUtil.join(this.appPrefixes, ", ")
                    + "] ...");
          } else if (this.useAppJarNames()) {
            log.info(
                "Looping over Java archive analyzers to separate application and dependency code"
                    + " using filename pattern(s) ["
                    + this.appJarNames.toString(", ")
                    + "] ...");
          }

          // Loop over all analyzers
          for (FileAnalyzer fa2 : analyzers) {

            try {
              if (fa2 instanceof JarAnalyzer) {
                final JarAnalyzer ja = (JarAnalyzer) fa2;

                // Prefixes
                if (this.useAppPrefixes()) {
                  final Set<ConstructId> constructs =
                      JavaId.filter(fa2.getConstructs().keySet(), this.appPrefixes);

                  // Constructs match to the prefixes
                  if (constructs != null && constructs.size() > 0) {
                    app_constructs.addAll(constructs);

                    // Exclusively app constructs
                    if (constructs.size() == fa2.getConstructs().size()) {
                      log.info(
                          StringUtil.padLeft(++count, 4)
                              + " ["
                              + StringUtil.padLeft(ja.getFileName(), 30)
                              + "]: All ["
                              + fa2.getConstructs().size()
                              + "] constructs matched prefix(es): Constructs added to application,"
                              + " file NOT added as dependency");
                    }
                    // Mixed archive: Add as dependency
                    else {
                      log.info(
                          StringUtil.padLeft(++count, 4)
                              + " ["
                              + StringUtil.padLeft(ja.getFileName(), 30)
                              + "]: ["
                              + constructs.size()
                              + "/"
                              + fa2.getConstructs().size()
                              + "] constructs matched prefix(es): Constructs added to application,"
                              + " file added as dependency");
                      dep_files.put(ja.getPath(), ja);
                    }
                  }
                  // No constructs match to the prefixes
                  else {
                    log.info(
                        StringUtil.padLeft(++count, 4)
                            + " ["
                            + StringUtil.padLeft(ja.getFileName(), 30)
                            + "]: None of the ["
                            + fa2.getConstructs().size()
                            + "] constructs matched prefix(es): No constructs added to"
                            + " application, file added as dependency");
                    dep_files.put(ja.getPath(), ja);
                  }
                }
                // Jar name regex
                else if (this.useAppJarNames()) {
                  // Belongs to application
                  if (this.appJarNames.contains(
                      ja.getFileName(),
                      StringList.ComparisonMode.PATTERN,
                      StringList.CaseSensitivity.CASE_INSENSITIVE)) {
                    log.info(
                        StringUtil.padLeft(++count, 4)
                            + " ["
                            + StringUtil.padLeft(ja.getFileName(), 30)
                            + "]: Filename matches pattern(s), all of its ["
                            + fa2.getConstructs().size()
                            + "] constructs added to application");
                    app_constructs.addAll(fa2.getConstructs().keySet());
                  }
                  // Dependency
                  else {
                    log.info(
                        StringUtil.padLeft(++count, 4)
                            + " ["
                            + StringUtil.padLeft(ja.getFileName(), 30)
                            + "]: Filename does not match pattern(s), file added as dependency");
                    dep_files.put(ja.getPath(), ja);
                  }
                }
              }
              // Important: What is in java and class files is always part of the app
              else {
                app_constructs.addAll(fa2.getConstructs().keySet());
              }
            } catch (FileAnalysisException e) {
              log.error(e.getMessage(), e);
            }
          }
        }
        // No prefixes and jar name regex: Add all constructs to app
        else {
          try {
            app_constructs.addAll(fa.getConstructs().keySet());
          } catch (FileAnalysisException e) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }

    // 2) Analyze all of the JAR/WAR files
    final Set<JarAnalyzer> app_dependencies = new HashSet<JarAnalyzer>();

    final long timeout =
        this.vulasConfiguration.getConfiguration().getLong(CoreConfiguration.JAR_TIMEOUT, -1);
    final int no_threads = ThreadUtil.getNoThreads(this.vulasConfiguration, 2);

    final ArchiveAnalysisManager mgr =
        new ArchiveAnalysisManager(no_threads, timeout, false, this.getApplication());
    mgr.setKnownDependencies(this.getKnownDependencies());
    mgr.startAnalysis(dep_files, null);

    // Loop over all analyzers created above and add to app dependencies
    final Set<JarAnalyzer> analyzers = mgr.getAnalyzers();
    for (JarAnalyzer ja : analyzers) {

      try {
        // Prefixes can be used: Filter JAR constructs
        if (this.useAppPrefixes()) {
          final Set<ConstructId> constructs =
              JavaId.filter(ja.getConstructs().keySet(), this.appPrefixes);

          // Constructs match to the prefixes
          if (constructs != null && constructs.size() > 0) {
            app_constructs.addAll(constructs);

            // Exclusively app constructs
            if (constructs.size() == ja.getConstructs().size()) {
              log.info(
                  "All of the ["
                      + ja.getConstructs().size()
                      + "] constructs from ["
                      + ja.getFileName()
                      + "] matched to prefix ["
                      + StringUtil.join(this.appPrefixes, ", ")
                      + "]: Constructs added to application, file removed from dependencies");
            }
            // Mixed archive: Add as dependency
            else {
              log.info(
                  "["
                      + constructs.size()
                      + "/"
                      + ja.getConstructs().size()
                      + "] constructs from ["
                      + ja.getFileName()
                      + "] matched to prefix ["
                      + StringUtil.join(this.appPrefixes, ", ")
                      + "]: Constructs added to application, file kept as dependency");
              app_dependencies.add(ja);
            }
          }
          // No constructs match to the prefixes
          else {
            log.info(
                "None of the ["
                    + ja.getConstructs().size()
                    + "] constructs from ["
                    + ja.getFileName()
                    + "] matched to prefix ["
                    + StringUtil.join(this.appPrefixes, ", ")
                    + "]: No constructs added to application, file kept as dependency");
            app_dependencies.add(ja);
          }
        }
        // Prefixes cannot be used: Add JAR as dependency unless jar name regex exists
        else {
          if (this.useAppJarNames()
              && this.appJarNames.contains(
                  ja.getFileName(),
                  StringList.ComparisonMode.PATTERN,
                  StringList.CaseSensitivity.CASE_INSENSITIVE)) {
            app_constructs.addAll(ja.getConstructs().keySet());
          } else {
            app_dependencies.add(ja);
          }
        }
      } catch (FileAnalysisException e) {
        log.error(e.getMessage(), e);
      }
    }

    // Update application
    final Application a = this.getApplication();
    a.addConstructs(ConstructId.getSharedType(app_constructs));

    // Loop all JAR analyzers and add a corresponding dependency
    for (JarAnalyzer ja : app_dependencies) {
      try {
        final Dependency app_dep = new Dependency();
        app_dep.setLib(ja.getLibrary());
        app_dep.setApp(this.getApplication());
        app_dep.setFilename(ja.getFileName());
        app_dep.setPath(ja.getPath().toString());

        // Dependency known to a package manager (if any)
        Dependency known_dep = null;
        if (ja.getParent() != null) {
          known_dep = mgr.getKnownDependency(ja.getParent().getPath());
        } else {
          known_dep = mgr.getKnownDependency(ja.getPath());
        }

        // Take information from known dependency
        app_dep.setScope(known_dep != null ? known_dep.getScope() : Scope.RUNTIME);
        app_dep.setTransitive(
            ja.getParent() != null
                ? true
                : (known_dep != null ? known_dep.getTransitive() : false));
        app_dep.setDeclared(known_dep != null && ja.getParent() == null ? true : false);

        // Set the parent (if any)
        if (known_dep != null && known_dep.getParent() != null) {
          // Complete the draft parent dependency with library info
          for (JarAnalyzer ja2 : app_dependencies) {
            // TODO: As soon as we add the relative path to the JARAnalyzer, it must also be used to
            // uniquely identify the parent and set all its fields
            if (ja2.getPath().toString().equals(known_dep.getParent().getPath())) {
              known_dep.getParent().setLib(ja2.getLibrary());
              break;
            }
          }
          app_dep.setParent(known_dep.getParent());
        }

        a.addDependency(app_dep);
      } catch (FileAnalysisException e) {
        log.error(e.getMessage(), e);
      }
    }

    // Get a clean set of dependencies
    final Set<Dependency> no_dupl_deps =
        DependencyUtil.removeDuplicateLibraryDependencies(a.getDependencies());
    a.setDependencies(no_dupl_deps);

    // Fix parents on dependencies that were removed (this block should be removed once we use the
    // relativePath and we get a dependency tree representing the actual one
    for (Dependency d : a.getDependencies()) {
      if (d.getParent() != null) {
        for (Dependency existing : a.getDependencies()) {
          // TODO: as soon as we add the relative path to the JARAnalyzer, it must also be used to
          // uniquely identify the parent
          if (existing.getLib().getDigest().equals(d.getParent().getLib().getDigest())) {
            d.getParent().setLib(existing.getLib());
            d.getParent().setPath(existing.getPath());
            d.getParent().setFilename(existing.getFilename());
            break;
          }
        }
      }
    }

    // Check whether the parent-child dependency relationships are consistent
    final boolean consistent_deps = DependencyUtil.isValidDependencyCollection(a);
    if (!consistent_deps) {
      throw new GoalExecutionException(
          "Inconsistent application dependencies cannot be uploaded", null);
    }

    // Set the one to be returned
    this.setCompletedApplication(a);
  }
}
