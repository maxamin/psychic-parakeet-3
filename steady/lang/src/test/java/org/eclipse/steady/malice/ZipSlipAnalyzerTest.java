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
package org.eclipse.steady.malice;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class ZipSlipAnalyzerTest {

  @Test
  public void testZipSlipFile() {
    final MaliciousnessAnalyzer mala = new ZipSlipAnalyzer();

    MaliciousnessAnalysisResult is_mal;

    // Test the 4 archives from https://github.com/snyk/zip-slip-vulnerability

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip.zip"));
    assertEquals(1d, is_mal.getResult(), 0d);

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip-win.zip"));
    assertEquals(1d, is_mal.getResult(), 0d);

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip.tar"));
    assertEquals(1d, is_mal.getResult(), 0d);

    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip-win.tar"));
    assertEquals(1d, is_mal.getResult(), 0d);

    // A malicious JAR
    is_mal = mala.isMalicious(new File("src/test/resources/zip-slip.jar"));
    assertEquals(1d, is_mal.getResult(), 0d);

    // A benign ZIP
    is_mal = mala.isMalicious(new File("src/test/resources/no-zip-slip.zip"));
    assertEquals(0d, is_mal.getResult(), 0d);
  }
}
