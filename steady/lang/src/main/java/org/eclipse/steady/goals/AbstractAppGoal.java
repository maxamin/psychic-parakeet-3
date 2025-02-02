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
package org.eclipse.steady.goals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * Represents an analysis goal executed in the context of a given {@link Application}.
 * It deals particularly with application code and dependencies.
 */
public abstract class AbstractAppGoal extends AbstractGoal {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(AbstractAppGoal.class);

  /**
   * Maps file system paths to {@link Dependency}s.
   */
  private Map<Path, Dependency> knownDependencies = new HashMap<Path, Dependency>();

  private List<Path> searchPaths = new ArrayList<Path>();

  /**
   * <p>Constructor for AbstractAppGoal.</p>
   *
   * @param _type a {@link org.eclipse.steady.shared.enums.GoalType} object.
   */
  protected AbstractAppGoal(GoalType _type) {
    super(_type);
  }

  /**
   * <p>getApplicationContext.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  protected Application getApplicationContext() {
    return this.getGoalContext().getApplication();
  }

  /**
   * Returns known {@link Dependency}s.
   *
   * @see AbstractAppGoal#setKnownDependencies(Map)
   * @return a {@link java.util.Map} object.
   */
  public Map<Path, Dependency> getKnownDependencies() {
    return this.knownDependencies;
  }

  /**
   * Sets known {@link Dependency}s.
   *
   * Typically called by Vulas plugins for build tools (e.g., the Vulas Maven plugin),
   * where app dependencies are described by project meta information (e.g., the pom.xml file).
   *
   * @param _paths a {@link java.util.Map} object.
   */
  public void setKnownDependencies(Map<Path, Dependency> _paths) {
    this.knownDependencies = _paths;
  }

  /**
   * {@inheritDoc}
   *
   * Checks whether one or more {@link Path}s with application constructs, and one or more {@link Path}s
   * with dependencies are available.
   */
  @Override
  protected void prepareExecution() throws GoalConfigurationException {

    super.prepareExecution();

    // Ensure presence of application in goal context
    if (this.getApplicationContext() == null)
      throw new GoalConfigurationException(
          "Application context is required to execute goal ["
              + this.getGoalType().toString()
              + "]");

    try {
      // Check path(s) with app constructs
      this.addAppPaths(
          FileUtil.getPaths(
              this.getConfiguration().getStringArray(CoreConfiguration.APP_DIRS, null)));

      // Warn if there's no app path
      if (!this.hasAppPaths()) log.warn("No search path(s) provided");

      // Extract single WAR file
      // if(this.appPaths.size()==1 && FileUtil.hasFileExtension(this.appPaths.get(0), WAR_EXT)) {
      // this.handleAppWars();
      // }

      // Verify existance of configured space (if any)
      if (this.getGoalContext().hasSpace()) {
        try {
          final boolean space_exists =
              BackendConnector.getInstance()
                  .isSpaceExisting(this.getGoalContext(), this.getGoalContext().getSpace());
          if (!space_exists)
            throw new GoalConfigurationException(
                "Workspace ["
                    + this.getGoalContext().getSpace()
                    + "] cannot be verified: Not present in server");
        } catch (BackendConnectionException e) {
          throw new GoalConfigurationException(
              "Workspace ["
                  + this.getGoalContext().getSpace()
                  + "] cannot be verified:"
                  + e.getMessage());
        }
      }

      // Upload goal execution before actual analysis (and another time after goal completion)
      final boolean created = this.upload(true);
      if (!created)
        throw new GoalConfigurationException(
            "Upload of goal execution failed, aborting the goal execution...");
    }
    // Thrown by all methods related to updating/adding paths
    catch (IllegalArgumentException e) {
      throw new GoalConfigurationException(e.getMessage());
    }
  }

  /**
   * <p>getAppPaths.</p>
   *
   * @return a {@link java.util.List} object.
   */
  public List<Path> getAppPaths() {
    return this.searchPaths;
  }

  /**
   * <p>addAppPath.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public void addAppPath(Path _p) throws IllegalArgumentException {
    if (!FileUtil.isAccessibleDirectory(_p) && !FileUtil.isAccessibleFile(_p))
      log.warn("[" + _p + "] is not an accessible file or directory");
    else if (this.getAppPaths().contains(_p))
      log.debug(
          "[" + _p + "] is already part of application paths, and will not be added another time");
    else this.searchPaths.add(_p);
  }

  /**
   * <p>addAppPaths.</p>
   *
   * @param _paths a {@link java.util.Set} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public void addAppPaths(Set<Path> _paths) throws IllegalArgumentException {
    for (Path p : _paths) this.addAppPath(p);
  }

  /**
   * <p>hasAppPaths.</p>
   *
   * @return a boolean.
   */
  public boolean hasAppPaths() {
    return this.getAppPaths() != null && !this.getAppPaths().isEmpty();
  }

  /**
   * Loops over all app paths, extracts all WARs (if any), and adds their content
   * to the app and dep paths.
   */
  /*private void handleAppWars() {
  	for(int i=0; i<this.appPaths.size(); i++) {
  		final Path p = this.appPaths.get(i);
  		if(FileUtil.hasFileExtension(p, WAR_EXT)) {
  			try {
  				this.extractAppWar(p);
  				this.appPaths.remove(i); // Remove only if extraction was successful
  			} catch (IOException e) {
  				log.error("Error while extracting WAR [" + p + "]: " + e.getMessage(), e);
  			}
  		}
  	}
  }*/
}
