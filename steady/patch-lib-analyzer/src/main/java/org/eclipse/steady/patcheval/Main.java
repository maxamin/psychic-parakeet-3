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
package org.eclipse.steady.patcheval;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.patcheval.utils.PEConfiguration;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * <p>Main class.</p>
 */
public class Main {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * <p>main.</p>
   *
   * @param _args an array of {@link java.lang.String} objects.
   */
  public static void main(String[] _args) {
    // Prepare parsing of cmd line arguments
    final Options options = new Options();
    options.addOption("j", "job", false, "Run patch Eval as cron job");
    options.addOption(
        "folder", "folderPath", true, "where to save CSV, JSON files, and code snippets");
    options.addOption(
        "lang", "lang", true, "language for the bugs to be analyzed (allowed values: PY or JAVA)");
    options.addOption("h", "hour", true, "Delay for starting the job (hours)");
    options.addOption("p", "period", true, "The period between successive executions (in hours)");
    options.addOption("bug", "bug", true, "comma separared list of bugs to analyze");
    options.addOption(
        "f", "toFile", false, "save json results to file; otherwise upload to backend");
    options.addOption(
        "o",
        "overrideResults",
        false,
        "Delete all existing results before upload; otherwise only upload results for"
            + " AffectedLibraries not already existing in the backend");

    // the list of bug to analyze, upload flag, backend and cia to use can also be configured in a
    // property file

    // Parse exception
    try {
      // Parse cmd line arguments
      final CommandLineParser parser = new DefaultParser();
      final CommandLine cmd = parser.parse(options, _args);

      if (cmd.hasOption("bug"))
        VulasConfiguration.getGlobal()
            .setProperty(PEConfiguration.BUGID, cmd.getOptionValue("bug"));

      if (cmd.hasOption("folder"))
        VulasConfiguration.getGlobal()
            .setProperty(PEConfiguration.BASEFOLDER, cmd.getOptionValue("folder"));

      if (cmd.hasOption("lang"))
        VulasConfiguration.getGlobal()
            .setProperty(PEConfiguration.LANG, cmd.getOptionValue("lang"));

      if (cmd.hasOption("f"))
        VulasConfiguration.getGlobal()
            .setProperty(PEConfiguration.UPLOAD_RESULTS, Boolean.valueOf(false));

      if (cmd.hasOption("o"))
        VulasConfiguration.getGlobal()
            .setProperty(PEConfiguration.ADD_RESULTS, Boolean.valueOf(false));

      PE_Run pe = new PE_Run();

      if (cmd.hasOption("j")) {
        Integer delay = null;
        if (cmd.hasOption("h")) delay = Integer.parseInt(cmd.getOptionValue("h"));
        Integer period = null;
        if (cmd.hasOption("p")) period = Integer.parseInt(cmd.getOptionValue("p"));

        // Date date = new Date();
        // String zero = (date.getMinutes()>9)?"":"0";
        if (delay == null) {
          // throw new IllegalArgumentException("If you specify the -j option to run patch eval as a
          // periodic job, then the following options are mandatory: (h)our and (p)eriod");
          Main.log.info("Setting default vaules for delay: 0 hour");
          delay = 0;
        }
        if (period == null) {
          Main.log.info("Setting default vaules to run patch eval as a periodic job every 24h");
          period = 24;
        }
        Main.log.info("Starting PatchEval as cron job");
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

        Main.log.info(
            "Initial delay = "
                + delay
                + " hour(s); the job will start after a delay of  "
                + delay
                + " hour"
                + " with a period of "
                + period
                + " hours");
        // System.out.println("Current Time = "+date);

        scheduledThreadPool.scheduleAtFixedRate(pe, delay, period, TimeUnit.HOURS);
      } else {
        Thread t = new Thread(pe);
        t.start();
      }
    } catch (ParseException pe) {
      Main.log.error(pe.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("PatchEval", options);
    }
  }
}
