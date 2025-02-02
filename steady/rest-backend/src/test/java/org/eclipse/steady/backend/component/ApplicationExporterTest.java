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
package org.eclipse.steady.backend.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class ApplicationExporterTest {

  @Test
  public void testPartition() {
    // 1x10
    Set<List<Integer>> parts = ApplicationExporter.partition(this.createList(10), 1);
    int num = 0;
    assertEquals(1, parts.size());
    assertEquals(10, parts.iterator().next().size());

    // 0
    parts = ApplicationExporter.partition(null, 10);
    num = 0;
    assertEquals(0, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(false);
    }
    assertEquals(0, num);

    // 1x0
    parts = ApplicationExporter.partition(this.createList(0), 10);
    num = 0;
    assertEquals(1, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 0);
      num += part.size();
    }
    assertEquals(0, num);

    // 1x1
    parts = ApplicationExporter.partition(this.createList(1), 10);
    num = 0;
    assertEquals(1, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 1);
      num += part.size();
    }
    assertEquals(1, num);

    // 1x2
    parts = ApplicationExporter.partition(this.createList(2), 10);
    num = 0;
    assertEquals(1, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 2);
      num += part.size();
    }
    assertEquals(2, num);

    // 10x1
    parts = ApplicationExporter.partition(this.createList(10), 10);
    num = 0;
    assertEquals(10, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 1);
      num += part.size();
    }
    assertEquals(10, num);

    // 5x2
    parts = ApplicationExporter.partition(this.createList(10), 5);
    num = 0;
    assertEquals(5, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 2);
      num += part.size();
    }
    assertEquals(10, num);

    // 4x2 + 1x3 = 11
    parts = ApplicationExporter.partition(this.createList(11), 5);
    num = 0;
    assertEquals(5, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 2 || part.size() == 3);
      num += part.size();
    }
    assertEquals(11, num);

    // 4x2 + 1x4
    parts = ApplicationExporter.partition(this.createList(12), 5);
    num = 0;
    assertEquals(5, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 2 || part.size() == 4);
      num += part.size();
    }
    assertEquals(12, num);

    // 39x28 + 1x38 = 1030
    parts = ApplicationExporter.partition(this.createList(1130), 40);
    num = 0;
    assertEquals(40, parts.size());
    for (List<Integer> part : parts) {
      assertTrue(part.size() == 28 || part.size() == 38);
      num += part.size();
    }
    assertEquals(1130, num);
  }

  private List<Integer> createList(int _size) {
    final List<Integer> l = new LinkedList<Integer>();
    for (int i = 0; i < _size; i++) l.add(i);
    return l;
  }
}
