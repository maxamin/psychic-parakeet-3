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
package org.eclipse.steady.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class JarWriterTest implements JarEntryWriter {

  private int countCallbacks = 0;

  /**
   * Set callback counter to 0 before every instrumentation.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    this.countCallbacks = 0;
  }

  @Test
  public void testHasEntry() {
    try {
      final JarWriter jw = new JarWriter(Paths.get("./src/test/resources/boot-app.jar"));
      assertTrue(jw.hasEntry("BOOT-INF/"));
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void testRewrite() {
    try {
      // Create a JarWriter and do some settings
      final JarWriter jw = new JarWriter(Paths.get("./src/test/resources/examples.jar"));
      // jw.setCompress(ZipEntry.STORED);
      jw.addManifestEntry("Test", "JUnit test entry");
      jw.setClassifier(StringUtil.getRandonString(6));
      jw.addFile("", Paths.get("./src/test/resources/Callgraph.class"), true);
      jw.addFile("WEB-INF/lib", Paths.get("./src/test/resources/examples.jar"), true);

      assertEquals("F22A5E25F37455867B5C2CF476BAC25189AC2B28", jw.getSHA1());

      // Callback for .class files and rewrite
      jw.register(".*.class$", this);
      final Path rewritten = jw.rewrite(Paths.get("./target"));

      // Callbacks for 6 class files
      assertEquals(6, this.countCallbacks);

      // Create a new JarWriter and check whether is recognized as rewritten (the original SHA1 is
      // taken from the manifest)
      final JarWriter jw2 = new JarWriter(rewritten);
      assertTrue(jw2.isRewrittenByVulas());
      assertEquals("F22A5E25F37455867B5C2CF476BAC25189AC2B28", jw2.getSHA1());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test(expected = SecurityException.class)
  public void testRewriteVerifyError() throws Throwable {
    try {
      // Create a JarWriter and do some settings
      System.setProperty(CoreConfiguration.VERIFY_JARS, "true");
      JarWriter jw =
          new JarWriter(
              Paths.get("./src/test/resources/org.eclipse.equinox.cm_1.0.400.v20120319-2029.jar"));
      jw.addManifestEntry("Test", "JUnit test entry");
      jw.setClassifier("dummy");

      // Callback for .class files
      jw.register(".*.class$", this);

      // Throws exception
      jw.rewrite(null);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (JarAnalysisException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testRewriteVerifySkip() {
    try {
      // Create a JarWriter and do some settings
      System.setProperty(CoreConfiguration.VERIFY_JARS, "false");
      JarWriter jw =
          new JarWriter(
              Paths.get("./src/test/resources/org.eclipse.equinox.cm_1.0.400.v20120319-2029.jar"));
      jw.addManifestEntry("Test", "JUnit test entry");
      jw.setClassifier("dummy");

      // Callback for .class files
      jw.register(".*.class$", this);

      // Rewrite the file
      final Path rewritten = jw.rewrite(null);

      // Callbacks for 29 class files
      assertEquals(29, this.countCallbacks);

      // Create a new JarWriter and check whether is recognized as rewritten
      JarWriter jw2 = new JarWriter(rewritten);
      assertTrue(jw2.isRewrittenByVulas());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void testAppendToClasspath() {
    try {
      final Set<Path> cp = new HashSet<Path>();

      // Needs rewriting
      final Path with_mf_entry =
          Paths.get("./src/test/resources/org.apache.servicemix.bundles.jaxb-xjc-2.2.4_1.jar");
      Path appended_path = JarWriter.appendToClasspath(cp, with_mf_entry, true);
      assertTrue(!cp.contains(with_mf_entry));
      assertTrue(!with_mf_entry.equals(appended_path));

      // Does not need rewriting
      final Path without_mf_entry = Paths.get("./src/test/resources/junit-4.12.jar");
      appended_path = JarWriter.appendToClasspath(cp, without_mf_entry, true);
      assertTrue(cp.contains(without_mf_entry));
      assertTrue(without_mf_entry.equals(appended_path));
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Override
  public RewrittenJarEntry getInputStream(String _regex, JarEntry _entry) {
    // System.out.println("Callback for regex [" + _regex + "], jar entry [" + _entry.getName() +
    // "]");
    this.countCallbacks++;
    return null;
  }
}
