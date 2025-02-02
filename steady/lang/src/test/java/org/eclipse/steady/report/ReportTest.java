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
package org.eclipse.steady.report;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.Configuration;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.AbstractGoalTest;
import org.eclipse.steady.goals.GoalConfigurationException;
import org.eclipse.steady.goals.GoalContext;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.shared.connectivity.PathBuilder;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.ExemptionBug;
import org.eclipse.steady.shared.json.model.ExemptionScope;
import org.eclipse.steady.shared.json.model.ExemptionSet;
import org.eclipse.steady.shared.json.model.ExemptionUnassessed;
import org.eclipse.steady.shared.util.FileUtil;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;
import org.w3c.tidy.Tidy;

public class ReportTest extends AbstractGoalTest {

  /**
   * App creation results in the following two HTTP calls.
   * @param _a TODO
   */
  private void setupMockServices(Application _a) throws IOException {
    final String s_json = JacksonUtil.asJsonString(_a);
    String path = null;

    // Options app: 200
    path = "/backend" + PathBuilder.app(_a);
    whenHttp(server)
        .match(composite(method(Method.OPTIONS), uri(path)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    // Get historical vuln deps
    path = "/backend" + PathBuilder.appVulnDeps(_a, true, false, true);
    path = path.substring(0, path.indexOf('?'));
    whenHttp(server)
        .match(
            composite(
                method(Method.GET),
                uri(path),
                parameter("includeHistorical", "true"),
                parameter("includeAffected", "false"),
                parameter("includeAffectedUnconfirmed", "true")))
        .then(
            stringContent(FileUtil.readFile(Paths.get("./src/test/resources/vuln_deps_hist.json"))),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    // Get actual vuln deps
    path = "/backend" + PathBuilder.appVulnDeps(_a, false, true, true);
    path = path.substring(0, path.indexOf('?'));
    whenHttp(server)
        .match(
            composite(
                method(Method.GET),
                uri(path),
                parameter("includeHistorical", "false"),
                parameter("includeAffected", "true"),
                parameter("includeAffectedUnconfirmed", "true")))
        .then(
            stringContent(
                FileUtil.readFile(Paths.get("./src/test/resources/vuln_deps_actual.json"))),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));
  }

  /**
   * Two HTTP requests shall be made
   *
   * @throws GoalConfigurationException
   * @throws GoalExecutionException
   */
  @Test
  public void testReport() {
    try {
      // Mock REST services
      this.configureBackendServiceUrl(server);
      this.setupMockServices(this.testApp);

      // Exemptions
      vulasConfiguration.setProperty(
          ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.reason",
          "The vulnerable library with digest 6F1EBC is no problem because ...");
      vulasConfiguration.setProperty(
          ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.libraries",
          "6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10");

      vulasConfiguration.setProperty(
          ExemptionBug.CFG_PREFIX + ".CVE-2013-2186.reason",
          "Vulnerability CVE-2013-2186 is no problem because ...");
      vulasConfiguration.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2013-2186.libraries", "*");

      vulasConfiguration.setProperty(
          ExemptionBug.CFG_PREFIX + ".CVE-2019-1234.reason",
          "Vulnerability CVE-2019-1234 is no problem because ...");

      vulasConfiguration.setProperty(ExemptionScope.CFG, "teST, provIDED");
      vulasConfiguration.setProperty(ExemptionUnassessed.CFG, "knOWN");

      final Configuration cfg = vulasConfiguration.getConfiguration();

      final GoalContext goal_context = new GoalContext();
      goal_context.setVulasConfiguration(vulasConfiguration);
      goal_context.setTenant(this.testTenant);
      goal_context.setSpace(this.testSpace);
      goal_context.setApplication(this.testApp);

      final Report report = new Report(goal_context, this.testApp, null);

      // Set all kinds of exceptions
      report.setExceptionThreshold(
          cfg.getString(CoreConfiguration.REP_EXC_THRESHOLD, Report.THRESHOLD_ACT_EXE));

      // Exemptions
      report.setExemptions(ExemptionSet.createFromConfiguration(cfg));

      // Fetch the vulns
      report.fetchAppVulnerabilities();

      // Loop over vulnerabilities
      report.processVulnerabilities();

      final Path report_dir = Paths.get("./target/vulas/report");
      if (!report_dir.toFile().exists()) report_dir.toFile().mkdirs();

      report.writeResult(report_dir);

      // Check that files exist
      assertTrue(FileUtil.isAccessibleFile(report_dir.resolve(Report.REPORT_FILE_HTML)));
      assertTrue(FileUtil.isAccessibleFile(report_dir.resolve(Report.REPORT_FILE_XML)));
      assertTrue(FileUtil.isAccessibleFile(report_dir.resolve(Report.REPORT_FILE_JSON)));

      // Validate Html
      final Tidy tidy = new Tidy();
      tidy.parse(
          new ByteArrayInputStream(
              FileUtil.readInputStream(
                  new FileInputStream(
                      new File("./target/vulas/report/" + Report.REPORT_FILE_HTML)))),
          new FileOutputStream(new File("./target/jtidy-html.txt")));

      // Allow no errors
      assertEquals(0, tidy.getParseErrors());

      // Allow just the following 3 warnings (interestingly, a missing </table> will only result in
      // additional warnings)
      // line 361 column 41 - Warning: <td> attribute "width" has invalid value "25%"
      // line 381 column 41 - Warning: <td> attribute "width" has invalid value "75%"
      // line 1 column 1 - Warning: html doctype doesn't match content
      assertEquals(3, tidy.getParseWarnings());

      // Check the HTTP calls made
      String path = "/backend" + PathBuilder.appVulnDeps(this.testApp, true, false, true);
      path = path.substring(0, path.indexOf('?'));
      verifyHttp(server)
          .times(
              1,
              method(Method.GET),
              uri(path),
              parameter("includeHistorical", "true"),
              parameter("includeAffected", "false"),
              parameter("includeAffectedUnconfirmed", "true"));
      verifyHttp(server)
          .times(
              1,
              method(Method.GET),
              uri(path),
              parameter("includeHistorical", "false"),
              parameter("includeAffected", "true"),
              parameter("includeAffectedUnconfirmed", "true"));
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
