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
package org.eclipse.steady.java.gradle;

import org.eclipse.steady.goals.AbstractAppGoal;
import org.eclipse.steady.goals.BomGoal;
import org.eclipse.steady.shared.enums.Scope;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.json.model.Library;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.eclipse.steady.shared.util.VulasConfiguration;

import org.eclipse.steady.java.gradle.VulasPluginCommon.ProjectOutputTypes;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GradlePluginApp extends AbstractVulasTask {

  private static final Map<ProjectOutputTypes, String> defaultConfigurations;

  static {
    defaultConfigurations = new HashMap<>();
    defaultConfigurations.put(ProjectOutputTypes.JAR, "runtimeClasspath");
    defaultConfigurations.put(ProjectOutputTypes.AAR, "releaseRuntimeClasspath");
    defaultConfigurations.put(ProjectOutputTypes.APK, "releaseRuntimeClasspath");
  }

  private Path tmpPath = null;

  @Override
  protected void createGoal() {
    this.goal = new BomGoal();
  }

  @Override
  protected void executeGoal() throws Exception {

    if (projectOutputType == null) {
      getLogger().quiet("Skip vulas-app in project {}", project.getName());
      return;
    }

    String configurationName =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString("vulas.gradle.configuration." + projectOutputType.toString().toLowerCase());

    if (configurationName == null) {
      configurationName = defaultConfigurations.get(projectOutputType);
    }

    Configuration configToAnalyze = project.getConfigurations().getAt(configurationName);
    getLogger().quiet("Resolving configuration {}", configToAnalyze.getName());

    // DependencyResolver.resolve(configToAnalyze);

    Set<ResolvedArtifactResult> artifacts = DependencyResolver.resolve(configToAnalyze);
    Set<ResolvedArtifactResult> direct_artifacts =
        DependencyResolver.resolveDirectOnly(configToAnalyze);

    int count = 0;
    Dependency dep = null;
    // Path to dependency info
    final Map<Path, Dependency> dep_for_path = new HashMap<Path, Dependency>();
    Library lib = null;
    for (ResolvedArtifactResult a : artifacts) {

      ModuleComponentIdentifier ci =
          ((ModuleComponentArtifactIdentifier) a.getId()).getComponentIdentifier();

      lib = new Library();
      lib.setLibraryId(new LibraryId(ci.getGroup(), ci.getModule(), ci.getVersion()));

      // Create dependency and put into map
      // TODO: hardcoded runtime scope, how to match gradle configs to maven scopes?
      dep =
          new Dependency(
              this.goal.getGoalContext().getApplication(),
              lib,
              Scope.RUNTIME,
              direct_artifacts.contains(a),
              null,
              a.getFile().getPath());
      dep_for_path.put(a.getFile().toPath(), dep);
    }

    ((AbstractAppGoal) this.goal).setKnownDependencies(dep_for_path);

    goal.executeSync();

    cleanupTempFiles();
  }

  private File unpackAar(File aarFile) throws IOException {

    final int BUFFER = 2048;

    if (tmpPath == null) {
      tmpPath = Files.createTempDirectory("vulas");
    }

    String classesJarName = aarFile.getName().replaceAll("\\.aar$", "-classes.jar");

    ZipFile zipfile = new ZipFile(aarFile);
    ZipEntry entry;

    BufferedInputStream src = null;
    BufferedOutputStream dst = null;

    entry = zipfile.getEntry("classes.jar");

    if (entry == null) {
      getLogger().quiet("No classes.jar present in {}", aarFile.getName());
      return null;
    }

    getLogger().quiet("Extracring classes.jar from {}", aarFile.getName());

    File dstFile = new File(tmpPath.toString(), classesJarName);
    src = new BufferedInputStream(zipfile.getInputStream(entry));

    int count;
    byte data[] = new byte[BUFFER];

    dst = new BufferedOutputStream(new FileOutputStream(dstFile), BUFFER);
    while ((count = src.read(data, 0, BUFFER)) != -1) {
      dst.write(data, 0, count);
    }

    dst.flush();
    dst.close();
    src.close();

    return dstFile;
  }

  private void cleanupTempFiles() throws IOException {

    if (tmpPath == null) {
      return;
    }

    Files.walkFileTree(
        tmpPath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }
}
