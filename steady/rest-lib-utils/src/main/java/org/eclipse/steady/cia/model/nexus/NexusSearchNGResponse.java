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
package org.eclipse.steady.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusSearchNGResponse class.</p>
 */
@XmlRootElement(name = "searchNGResponse")
public class NexusSearchNGResponse {

  NexusNGData data;

  /**
   * <p>Getter for the field <code>data</code>.</p>
   *
   * @return a {@link org.eclipse.steady.cia.model.nexus.NexusNGData} object.
   */
  public NexusNGData getData() {
    return data;
  }

  /**
   * <p>Setter for the field <code>data</code>.</p>
   *
   * @param data a {@link org.eclipse.steady.cia.model.nexus.NexusNGData} object.
   */
  public void setData(NexusNGData data) {
    this.data = data;
  }
}
