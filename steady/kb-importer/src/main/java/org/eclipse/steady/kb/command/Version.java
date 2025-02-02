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
package org.eclipse.steady.kb.command;

import java.util.HashMap;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.kb.Main;
import org.eclipse.steady.kb.exception.ValidationException;

/**
 * version command
 */
public class Version implements Command {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** {@inheritDoc} */
  @Override
  public void run(HashMap<String, Object> args) {
    String vulasRelease = Main.class.getPackage().getImplementationVersion();
    if (StringUtils.isEmpty(vulasRelease)) {
      log.error("unable to get vulas version");
    } else {
      log.info(vulasRelease);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Options getOptions() {
    return new Options();
  }

  /** {@inheritDoc} */
  @Override
  public void validate(HashMap<String, Object> args) throws ValidationException {
    // Nothing to validate as help does not have any args
  }

  /** {@inheritDoc} */
  @Override
  public Command.NAME getCommandName() {
    return Command.NAME.VERSION;
  }
}
