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
package org.eclipse.steady.cg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.steady.shared.json.model.ConstructId;

import com.ibm.wala.util.graph.Graph;

/**
 * To implement different algorithms of computing all paths
 */
public abstract class AbstractGetPaths {

  protected Graph<Integer> graph = null;
  protected ArrayList<ConstructId> nodeId = null;

  /**
   * <p>Constructor for AbstractGetPaths.</p>
   *
   * @param _graph a {@link com.ibm.wala.util.graph.Graph} object.
   * @param _nodeid a {@link java.util.ArrayList} object.
   */
  public AbstractGetPaths(Graph<Integer> _graph, ArrayList<ConstructId> _nodeid) {
    this.graph = _graph;
    this.nodeId = _nodeid;
  }

  /**
   * <p>getAllPaths.</p>
   *
   * @param _src a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @param _tgt a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @return a {@link java.util.HashSet} object.
   */
  public abstract HashSet<LinkedList<ConstructId>> getAllPaths(ConstructId _src, ConstructId _tgt);
}
