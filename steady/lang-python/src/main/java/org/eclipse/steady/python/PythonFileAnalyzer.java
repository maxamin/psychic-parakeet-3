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
package org.eclipse.steady.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.Construct;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.util.DirUtil;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StringUtil;

/**
 * Wraps the two Python File Analyzers into one.
 */
public class PythonFileAnalyzer implements FileAnalyzer {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private FileAnalyzer analyzer = null;

  private File file = null;

  private Map<ConstructId, Construct> constructs = null;

  /** {@inheritDoc} */
  @Override
  public String[] getSupportedFileExtensions() {
    return new String[] {"py"};
  }

  /** {@inheritDoc} */
  @Override
  public boolean canAnalyze(File _file) {
    final String ext = FileUtil.getFileExtension(_file);
    if (ext == null || ext.equals("")) return false;
    for (String supported_ext : this.getSupportedFileExtensions()) {
      if (supported_ext.equalsIgnoreCase(ext)) return true;
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void analyze(final File _file) throws FileAnalysisException {
    if (!FileUtil.isAccessibleFile(_file.toPath()))
      throw new IllegalArgumentException("[" + _file + "] does not exist or is not readable");
    this.file = _file;
  }

  /**
   * Returns true if the top-most element of the stack is of type {@link ConstructType#MODU}, false otherwise.
   */
  static boolean isTopOfType(Stack<PythonId> _context, PythonId.Type _type) {
    if (_context == null) throw new IllegalArgumentException("Stack argument is null");
    if (_context.isEmpty()) return false;
    final PythonId id = (PythonId) _context.peek();
    return id.getType().equals(_type);
  }

  /**
   * Returns true if the top-most element of the stack is of any of the given {@link PythonId#Type}s, false otherwise.
   */
  static boolean isTopOfType(Stack<PythonId> _context, PythonId.Type[] _types) {
    if (_context == null) throw new IllegalArgumentException("Stack argument is null");
    if (_context.isEmpty()) return false;
    final PythonId id = (PythonId) _context.peek();
    for (PythonId.Type t : _types) if (id.getType().equals(t)) return true;
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
    if (this.constructs == null) {
      analyzer = PythonFileAnalyzer.createAnalyzer(this.file);
      analyzer.analyze(this.file);
      this.constructs = analyzer.getConstructs();
    }
    return analyzer.getConstructs();
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
    return this.getConstructs().containsKey(_id);
  }

  /** {@inheritDoc} */
  @Override
  public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
    return this.getConstructs().get(_id);
  }

  /**
   * {@inheritDoc}
   *
   * The nested {@link Python3FileAnalyzer} is completely hidden.
   */
  @Override
  public boolean hasChilds() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * The nested {@link Python3FileAnalyzer} is completely hidden.
   */
  @Override
  public Set<FileAnalyzer> getChilds(boolean _recursive) {
    return null;
  }

  /**
   * Creates a {@link PythonId} of type {@link ConstructType#MODU} for the given py file.
   *
   * @param _file a {@link java.io.File} object.
   * @return a {@link org.eclipse.steady.python.PythonId} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static PythonId getModule(File _file) throws IllegalArgumentException {
    if (!FileUtil.hasFileExtension(_file.toPath(), new String[] {"py"})) {
      throw new IllegalArgumentException(
          "Expected file with file extension [py], got [" + _file.toString() + "]");
    }

    final Path p = _file.toPath().toAbsolutePath();

    // Add file name w/o extension to qname components
    final String module_name = FileUtil.getFileName(p.toString(), false);

    // Search upwards until there's no __init__.py anymore, and add directory names to the qname
    // components
    final List<String> package_name = new ArrayList<String>();
    Path search_path = p.getParent();
    while (DirUtil.containsFile(search_path.toFile(), "__init__.py")
        && search_path.getNameCount() > 1) {
      package_name.add(0, search_path.getFileName().toString());
      search_path = search_path.getParent();
    }

    // Create the package (if any), the module and return the latter
    PythonId pack = null;
    if (!package_name.isEmpty())
      pack = new PythonId(null, PythonId.Type.PACKAGE, StringUtil.join(package_name, "."));
    return new PythonId(pack, PythonId.Type.MODULE, module_name);
  }

  /**
   * Checks for syntax that is specific to Python2.
   *
   * TODO: To be completed according to https://docs.python.org/release/3.0.1/whatsnew/3.0.html.
   *
   * As of today, the method searches for the following:
   * - print statement becomes a function: print "bla" --> print("bla")
   * - raw_input does not exist anymore: raw_input() --> input("bla: ")
   */
  static final Pattern[] PY2_PATTERNS =
      new Pattern[] {Pattern.compile("^\\s*print\\s+\".*$"), Pattern.compile("^.*raw_input\\(.*$")};

  static final Pattern[] PY35_ASYNC_PATTERNS =
      new Pattern[] {Pattern.compile("^.*async\\s*def.*$")};

  static final Pattern[] COMMENT_PATTERNS = new Pattern[] {Pattern.compile("^\\s*#.*$")};

  /**
   * <p>createAnalyzer.</p>
   *
   * @param _file a {@link java.io.File} object.
   * @return a {@link org.eclipse.steady.FileAnalyzer} object.
   */
  public static FileAnalyzer createAnalyzer(final File _file) {
    try (final InputStream is = new FileInputStream(_file)) {
      return PythonFileAnalyzer.createAnalyzer(is);
    } catch (IOException e) {
      log.error(
          e.getClass().getSimpleName()
              + " when creating analyzer for file ["
              + _file.toPath().toAbsolutePath()
              + "]: "
              + e.getMessage(),
          e);
    }
    return null;
  }

  /**
   * Reads the input stream line by line in order to decide which {@link FileAnalyzer} to take.
   * Defaults to {@link Python335FileAnalyzer}.
   *
   * @param _is a {@link java.io.InputStream} object.
   * @return a {@link org.eclipse.steady.FileAnalyzer} object.
   * @throws java.io.IOException if any.
   */
  public static FileAnalyzer createAnalyzer(InputStream _is) throws IOException {
    FileAnalyzer fa = null;
    final BufferedReader isr = new BufferedReader(new InputStreamReader(_is));
    String line = null;
    int line_count = 0;
    while ((line = isr.readLine()) != null) {
      line_count++;
      // No comment
      if (!StringUtil.matchesPattern(line, COMMENT_PATTERNS)) {
        // Py2
        if (StringUtil.matchesPattern(line, PY2_PATTERNS)) {
          log.info(
              "Found one of the Python 2 patterns ["
                  + StringUtil.join(PY2_PATTERNS, ", ")
                  + "] in line ["
                  + line_count
                  + "]: "
                  + line.trim());
          fa = new Python335FileAnalyzer();
          break;
        }
        // Py 35+
        else if (StringUtil.matchesPattern(line, PY35_ASYNC_PATTERNS)) {
          log.info(
              "Found one of the Python 3.5 patterns ["
                  + StringUtil.join(PY35_ASYNC_PATTERNS, ", ")
                  + "] in line ["
                  + line_count
                  + "]: "
                  + line.trim());
          fa = new Python3FileAnalyzer();
          break;
        }
      }
    }

    // Default to 335
    if (fa == null) fa = new Python335FileAnalyzer();

    return fa;
  }
}
