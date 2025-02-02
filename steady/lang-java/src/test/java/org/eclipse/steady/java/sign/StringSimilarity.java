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
package org.eclipse.steady.java.sign;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public abstract class StringSimilarity {

  protected abstract double calculateSimilarity(String left, String right);

  @Test
  public void emptyStringsShouldBeSimilar() throws Exception {
    assertThat(calculateSimilarity("", ""), is(1.0));
  }

  @Test
  public void identicalStringsShouldBeSimilar() throws Exception {
    assertThat(calculateSimilarity("change distiller", "change distiller"), is(1.0));
  }
}
