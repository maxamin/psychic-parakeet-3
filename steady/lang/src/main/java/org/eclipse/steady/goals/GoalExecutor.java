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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;

/**
 * <p>GoalExecutor class.</p>
 */
public class GoalExecutor {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(GoalExecutor.class);

  private static GoalExecutor instance = null;

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link org.eclipse.steady.goals.GoalExecutor} object.
   */
  public static synchronized GoalExecutor getInstance() {
    if (instance == null) instance = new GoalExecutor();
    return instance;
  }

  private ExecutorService pool = null;

  private GoalExecutor() {
    this.pool = Executors.newFixedThreadPool(4); // newSingleThreadExecutor();
  }

  /**
   * <p>execute.</p>
   *
   * @param _goal a {@link org.eclipse.steady.goals.AbstractGoal} object.
   */
  public void execute(AbstractGoal _goal) {
    this.pool.execute(_goal);
  }
}
