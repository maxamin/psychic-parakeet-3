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
package org.eclipse.steady.backend.util;

import java.nio.charset.StandardCharsets;

import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.util.DigestUtil;

/**
 * <p>TokenUtil class.</p>
 */
public class TokenUtil {

  /**
   * Returns a random 64-char long token (MD5 hash generated over a randum
   * number and the current time milliseconds). Used for generating
   * {@link org.eclipse.steady.backend.model.Tenant} and
   * {@link org.eclipse.steady.backend.model.Space} tokens.
   *
   * @return a {@link java.lang.String} object.
   */
  public static String generateToken() {
    long rnd = (long) Math.abs(Math.random() * 100000000);
    long ms = System.currentTimeMillis();
    return DigestUtil.getDigestAsString(
        rnd + "-" + ms, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
  }
}
