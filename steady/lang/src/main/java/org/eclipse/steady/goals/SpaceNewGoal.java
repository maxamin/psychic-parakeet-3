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

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.model.Space;

/**
 * <p>SpaceNewGoal class.</p>
 */
public class SpaceNewGoal extends AbstractSpaceGoal {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(SpaceNewGoal.class);

  private Space createdSpace = null;

  /**
   * <p>Constructor for SpaceNewGoal.</p>
   */
  public SpaceNewGoal() {
    super(GoalType.SPACENEW);
  }

  /** {@inheritDoc} */
  @Override
  protected void executeTasks() throws Exception {
    final Space s = new Space();
    this.updateFromConfig(s);

    // Warn that any provided space token is ignored when creating a new space
    if (s.isValidSpaceToken())
      log.warn(
          "Upon space creation a new token will be generated, the current token ["
              + s.getSpaceToken()
              + "] will be ignored");

    // Check that name and description are provided
    if (!s.hasNameAndDescription()) {
      this.skipGoalUpload();
      throw new GoalExecutionException(
          "Space creation requires name and description, adjust the configuration accordingly",
          null);
    }

    // Create space and print token to be used in configuration
    this.createdSpace = BackendConnector.getInstance().createSpace(this.getGoalContext(), s);
    if (this.createdSpace == null) {
      log.error("Space creation failed");
    } else {
      log.info(
          "Space creation succeeded: Use space token ["
              + this.createdSpace.spaceToken
              + "] for configuration setting ["
              + CoreConfiguration.SPACE_TOKEN
              + "]");

      // Update context so that goal execution is uploaded properly
      this.getGoalContext().setSpace(this.createdSpace);
    }
  }

  /**
   * <p>getResultObject.</p>
   *
   * @return a {@link java.lang.Object} object.
   */
  protected Object getResultObject() {
    return this.createdSpace;
  }
}
