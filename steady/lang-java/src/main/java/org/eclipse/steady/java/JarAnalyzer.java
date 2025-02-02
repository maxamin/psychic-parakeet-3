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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.Construct;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.java.monitor.ClassVisitor;
import org.eclipse.steady.java.monitor.InstrumentationControl;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.enums.PropertySource;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Library;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.eclipse.steady.shared.json.model.Property;
import org.eclipse.steady.shared.util.FileUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Analyzes a single Java archives as to identify (and potentially instrument) all its constructs.
 */
@NotThreadSafe
public class JarAnalyzer implements Callable<FileAnalyzer>, JarEntryWriter, FileAnalyzer {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(JarAnalyzer.class);

  private static final ClassPool CLASSPOOL = ClassPool.getDefault();

  // The following can be set statically, and will be given to all ClassVisitors (for being included
  // in the instrumentation)
  private static Application APP_CTX = null;

  protected Set<ConstructId> constructs = null;
  private Map<ConstructId, Construct> constructBodies = null;

  private Set<String> classNames = new HashSet<String>();
  protected String url = null;
  protected JarFile jar = null;
  protected boolean instrument =
      false; // Set by the constructor, determines whether or not the class methods/constructors are
  // instrumented
  protected boolean rename = false; // Appends app context to original file name:
  // <originalJarName>-vulas-<appGroupId>-<appArtifactId>-<appVersion>.jar

  protected int classCount = 0;
  protected int enumCount = 0;
  protected int interfaceCount = 0;

  private Map<JavaId, ClassVisitor> instrumentedClasses = new HashMap<JavaId, ClassVisitor>();

  protected Path workDir = null; // To where modified JARs are written

  private LibraryId libraryId = null;

  private Set<LibraryId> bundledLibraryIds = new HashSet<LibraryId>();

  protected JarWriter jarWriter = null;

  private JarAnalyzer parent = null;

  protected InstrumentationControl instrControl = null;

  /** {@inheritDoc} */
  @Override
  public String[] getSupportedFileExtensions() {
    return new String[] {"jar"};
  }

  /**
   * Returns true if the archive has file extension 'jar' and does not contain
   * a {@link JarEntry} 'BOOT-INF/', false otherwise.
   */
  @Override
  public boolean canAnalyze(File _file) {
    final String ext = FileUtil.getFileExtension(_file);
    if (ext == null || ext.equals("")) return false;
    for (String supported_ext : this.getSupportedFileExtensions()) {
      if (supported_ext.equalsIgnoreCase(ext)) {
        try {
          final JarWriter jw = new JarWriter(_file.toPath());
          if (jw.hasEntry("BOOT-INF/")) {
            return false;
          } else {
            return true;
          }
        } catch (IOException ioe) {
          log.error(ioe.getMessage());
        }
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void analyze(final File _file) throws FileAnalysisException {
    try {
      this.jar = new JarFile(_file, false, java.util.zip.ZipFile.OPEN_READ);
      this.jarWriter = new JarWriter(_file.toPath());
      this.url = _file.getAbsolutePath().toString();
    } catch (IllegalStateException e) {
      log.error("IllegalStateException when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    } catch (IOException e) {
      log.error("IOException when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Exception when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    }
  }

  /**
   * <p>Setter for the field <code>instrument</code>.</p>
   *
   * @param _instrument a boolean.
   */
  public void setInstrument(boolean _instrument) {
    this.instrument = _instrument;
    if (this.instrument) {
      this.instrControl = InstrumentationControl.getInstance(this.url);
    }
  }

  /**
   * <p>getPath.</p>
   *
   * @return a {@link java.nio.file.Path} object.
   */
  public Path getPath() {
    return Paths.get(this.url);
  }

  /**
   * <p>Setter for the field <code>parent</code>.</p>
   *
   * @param ja a {@link org.eclipse.steady.java.JarAnalyzer} object.
   */
  public void setParent(JarAnalyzer ja) {
    this.parent = ja;
  }

  /**
   * <p>Getter for the field <code>parent</code>.</p>
   *
   * @return a {@link org.eclipse.steady.java.JarAnalyzer} object.
   */
  public JarAnalyzer getParent() {
    return this.parent;
  }

  /**
   * Returns the size of the original JAR file (before instrumentation).
   *
   * @see #getInstrumentedFileSize()
   * @return a long.
   */
  public long getFileSize() {
    return this.jarWriter.getFileSize();
  }

  /**
   * Returns the size of the instrumented JAR file (or -1 if no instrumentation took place).
   *
   * @see #getFileSize()
   * @return a long.
   */
  public long getInstrumentedFileSize() {
    return this.jarWriter.getInstrumentedFileSize();
  }

  /**
   * Specifies the work directory into which instrumented JARs are written. If null, a temporary directory will be created.
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  public void setWorkDir(Path _p) {
    this.workDir = _p;
  }

  /**
   * Determines whether the instrumented JAR is renamed or not. If yes, the new file name follows the following format:
   * - If app context is provided: [originalJarName]-vulas-[appGroupId]-[appArtifactId]-[appVersion].jar
   * - Otherwise: [originalJarName]-steady-instr.jar
   *
   * @param _b a boolean.
   */
  public void setRename(boolean _b) {
    this.rename = _b;
  }

  /**
   * Sets the Maven Id for the JAR to be analyzed. The Maven ID is already known in some contexts (e.g., during Maven plugin execution).
   *
   * @param _id a {@link org.eclipse.steady.shared.json.model.LibraryId} object.
   */
  public void setLibraryId(LibraryId _id) {
    this.libraryId = _id;
  }

  /**
   * Returns a {@link Library} representing the analyzed Java archive.
   *
   * @throws org.eclipse.steady.FileAnalysisException
   * @return a {@link org.eclipse.steady.shared.json.model.Library} object.
   */
  public Library getLibrary() throws FileAnalysisException {
    final Library lib = new Library(this.getSHA1());

    lib.setDigestAlgorithm(DigestAlgorithm.SHA1);
    lib.setConstructs(this.getSharedConstructs());
    lib.setLibraryId(this.libraryId);

    if (this.bundledLibraryIds != null && !this.bundledLibraryIds.isEmpty())
      lib.setBundledLibraryIds(this.bundledLibraryIds);

    final Set<Property> p = new HashSet<Property>();
    if (this.jarWriter.getOriginalManifest() != null) {
      for (Object key : this.jarWriter.getOriginalManifest().getMainAttributes().keySet()) {
        p.add(
            new Property(
                PropertySource.JAVA_MANIFEST,
                key.toString(),
                this.jarWriter.getOriginalManifest().getMainAttributes().get(key).toString()));
      }
    }
    lib.setProperties(p);

    return lib;
  }

  /**
   * Returns the SHA1 digest of the JAR. Either taken from the manifest (entry VULAS-originalSHA1, in case the original JAR has been instrumented
   * offline), or by computing it on the fly.
   *
   * @return the SHA1 digest of the JAR
   */
  public synchronized String getSHA1() {
    return this.jarWriter.getSHA1();
  }

  /**
   * <p>getFileName.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFileName() {
    return this.jarWriter.getOriginalJarFileName().toString();
  }

  /**
   * This method is called by {@link ArchiveAnalysisManager}.
   *
   * @return a {@link org.eclipse.steady.FileAnalyzer} object.
   */
  public FileAnalyzer call() {
    try {
      this.getSHA1();
      this.getConstructIds();
      this.getChilds(true);
      if (this.instrument) {
        try {
          this.createInstrumentedArchive();
        } catch (JarAnalysisException jae) {
          JarAnalyzer.log.error(this.toString() + ": " + jae.getMessage());
        }
      }
    } catch (Exception e) {
      if (e instanceof NullPointerException) {
        JarAnalyzer.log.error(
            this.toString() + ": [" + e.getClass().getSimpleName() + "] during analysis");
        e.printStackTrace();
      } else {
        JarAnalyzer.log.error(
            this.toString()
                + ": ["
                + e.getClass().getSimpleName()
                + "] during analysis: "
                + e.getMessage());
      }
    }
    return this;
  }

  /**
   * See here: http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
   *
   * @throws org.eclipse.steady.java.JarAnalysisException
   */
  protected void createInstrumentedArchive() throws JarAnalysisException {

    // Additional manifest file entries
    this.jarWriter.addManifestEntry(
        "Steady-classInstrStats",
        "["
            + this.instrControl.countClassesTotal()
            + " total, "
            + this.instrControl.countClassesInstrumentedAlready()
            + " existed, "
            + this.instrControl.countClassesInstrumentedSuccess()
            + " ok, "
            + this.instrControl.countClassesInstrumentedFailure()
            + " err]");
    this.jarWriter.addManifestEntry(
        "Steady-constructStats", "[" + constructs.size() + " constructs]");
    if (JarAnalyzer.getAppContext() != null)
      this.jarWriter.addManifestEntry(
          "Steady-appContext",
          JarAnalyzer.getAppContext().getMvnGroup()
              + ":"
              + JarAnalyzer.getAppContext().getArtifact()
              + ":"
              + JarAnalyzer.getAppContext().getVersion());

    // Register this JarAnalyzer for callbacks
    this.jarWriter.register(".*.class$", this);

    // Rename
    if (this.rename) {
      this.jarWriter.setClassifier("steady-instr");
    }

    // Rewrite
    this.jarWriter.rewrite(this.workDir);

    // Stats
    this.instrControl.logStatistics();
  }

  /**
   * <p>getInstrumentedArchive.</p>
   *
   * @return a {@link java.io.File} object.
   */
  public final File getInstrumentedArchive() {
    return this.jarWriter.getRewrittenJarFile();
  }

  /**
   * Returns the class names for all class files found in the given archive.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<String> getClassNames() {
    // Trigger the scan (in case not yet done)
    this.hasJARConstructs();
    return this.classNames;
  }

  /**
   * <p>hasJARConstructs.</p>
   *
   * @return a boolean.
   */
  public boolean hasJARConstructs() {
    return this.getConstructIds().size() > 0;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChilds() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Set<FileAnalyzer> getChilds(boolean _recursive) {
    return null;
  }

  /**
   * Identifies all {@link ConstructId}s of all methods and constructors.
   *
   * @return a {@link java.util.Set} object.
   */
  public synchronized Set<ConstructId> getConstructIds() {
    // this method is used to collect statistics about the analyzed jars but these are not available
    // (and thus skipped if the flag skipknownArchive is true
    // if(this.constructs==null && !uploadEnabledAndSkipKnownArchive()) {
    if (this.constructs == null) {
      this.constructs = new TreeSet<ConstructId>();

      final SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);

      // Loop all files in order to identify Java classes and bundled pom.xml files
      final Enumeration<JarEntry> en = this.jar.entries();
      while (en.hasMoreElements()) {
        final JarEntry je = en.nextElement();

        // .class files
        if (je.getName().endsWith(".class")) {

          // Ignore class files below META-INF/versions
          if (je.getName().startsWith("META-INF/versions/")) {
            JarAnalyzer.log.warn(
                "Multi-release JARs are not supported, skipping JAR entry [" + je.getName() + "]");
          }

          // Ignore package-info.class and module-info.class
          else if (je.getName().endsWith("package-info.class")
              || je.getName().endsWith("module-info.class")) {
            ;
          }

          // Build class identifier for every other .class file
          else {
            try {
              final String fqn = JarAnalyzer.getFqClassname(je.getName());
              this.classNames.add(fqn);
              JarAnalyzer.log.debug(
                  "JAR entry ["
                      + je.getName()
                      + "] transformed to Java class identifier ["
                      + fqn
                      + "]");
            } catch (IllegalArgumentException iae) {
              JarAnalyzer.log.warn(iae.getMessage());
            }
          }
        }

        // pom.xml files
        else if (je.getName().endsWith("pom.xml")) {
          try {
            final PomParser pp = new PomParser();
            final SAXParser saxParser = spf.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(pp);
            xmlReader.parse(new InputSource(this.jar.getInputStream(je)));
            if (pp.getLibraryId().isDefined()) this.bundledLibraryIds.add(pp.getLibraryId());
          } catch (ParserConfigurationException e) {
            JarAnalyzer.log.warn(
                "Parser configuration exception when parsing JAR entry ["
                    + je.getName()
                    + "]: "
                    + e.getMessage(),
                e);
          } catch (SAXException e) {
            JarAnalyzer.log.warn(
                "Exception when parsing JAR entry [" + je.getName() + "]: " + e.getMessage(), e);
          } catch (IOException e) {
            JarAnalyzer.log.warn(
                "I/O exception for JAR entry [" + je.getName() + "]: " + e.getMessage(), e);
          }
        }
      }

      //			// From where the classes will be loaded
      //			ClassPool cp = ClassPool.getDefault();
      //			// Must include the JAR file itself plus, optionally, other dependencies (read from a
      // static attribute)
      //			try {
      //				cp.insertClassPath(this.url);
      //			} catch (NotFoundException e) {
      //				JarAnalyzer.log.error("Error while adding JAR '" + this.url + "' to class path");
      //			}

      // Add the current JAR to the classpath
      try {
        JarAnalyzer.insertClasspath(this.url);
      } catch (NotFoundException e) {
        JarAnalyzer.log.error("Error while adding JAR [" + this.url + "] to class path");
      }

      // Visit all classes using Javassist (and instrument as many as possible - if requested)
      CtClass ctclass = null;
      ClassVisitor cv = null;

      for (String cn : this.classNames) {
        try {
          ctclass = JarAnalyzer.getClassPool().get(cn);

          // Ignore interfaces (no executable code) and enums (rarely containing executable code,
          // perhaps to be included later on)
          if (ctclass.isInterface()) {
            this.interfaceCount++;
          } else {

            if (ctclass.isEnum()) this.enumCount++;
            else this.classCount++;

            // Create ClassVisitor for the current Java class
            cv = new ClassVisitor(ctclass);
            this.constructs.addAll(cv.getConstructs());

            // Instrument (if requested and not blacklisted)
            if (this.instrument && !this.instrControl.isBlacklistedClass(cn)) {
              cv.setOriginalArchiveDigest(this.getSHA1());
              cv.setAppContext(JarAnalyzer.getAppContext());
              if (cv.isInstrumented())
                this.instrControl.updateInstrumentationStatistics(cv.getJavaId(), null);
              else {
                try {
                  cv.visitMethods(true);
                  cv.visitConstructors(true);
                  cv.finalizeInstrumentation();
                  this.instrumentedClasses.put(cv.getJavaId(), cv);
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(true));
                } catch (IOException ioe) {
                  JarAnalyzer.log.error(
                      "I/O exception while instrumenting class ["
                          + cv.getJavaId().getQualifiedName()
                          + "]: "
                          + ioe.getMessage());
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(false));
                } catch (CannotCompileException cce) {
                  JarAnalyzer.log.warn(
                      "Cannot compile instrumented class ["
                          + cv.getJavaId().getQualifiedName()
                          + "]: "
                          + cce.getMessage());
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(false));
                } catch (Exception e) {
                  JarAnalyzer.log.error(
                      e.getClass().getName()
                          + " occured while instrumenting class ["
                          + cv.getJavaId().getQualifiedName()
                          + "]: "
                          + e.getMessage());
                  this.instrControl.updateInstrumentationStatistics(
                      cv.getJavaId(), Boolean.valueOf(false));
                }
              }
            }
          }
          if (!this.instrument) {
            // only detach if no static instrumentation (otherwise it will fail
            // because the class was modified) in case the instrumentation is
            // performed the detach is done in
            // ClassVisitor.finalizeInstrumentation

            // Can throw a NPE according to
            // https://github.com/jboss-javassist/javassist/pull/395, esp. if
            // the same class is multiple times in the classpath, which is
            // caught by the catch clause below. Update to 3.29.0-GA ASAP
            ctclass.detach();
          }
        } catch (NotFoundException nfe) {
          JarAnalyzer.log.error(
              this.toString()
                  + ": NotFoundException while analyzing class ["
                  + cn
                  + "]: "
                  + nfe.getMessage());
          continue;
        } catch (RuntimeException re) {
          JarAnalyzer.log.error(
              this.toString()
                  + ": RuntimeException while analyzing class ["
                  + ctclass.getName()
                  + "]: "
                  + re.getMessage());
          continue;
        }
      }
      if (this.instrument)
        JarAnalyzer.log.info(
            this.toString()
                + ": classes and enums comprised/already-instr/instr/not-instr ["
                + this.instrControl.countClassesTotal()
                + "/"
                + this.instrControl.countClassesInstrumentedAlready()
                + "/"
                + this.instrControl.countClassesInstrumentedSuccess()
                + "/"
                + this.instrControl.countClassesInstrumentedFailure()
                + "], constructs comprised ["
                + constructs.size()
                + "], enums ["
                + enumCount
                + "], interfaces (ignored) ["
                + interfaceCount
                + "]");
      else
        JarAnalyzer.log.info(
            this.toString()
                + ": constructs comprised ["
                + constructs.size()
                + "], classes ["
                + this.classCount
                + "], enums ["
                + enumCount
                + "], interfaces (ignored) ["
                + interfaceCount
                + "]");
    }
    return this.constructs;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    final StringBuilder b = new StringBuilder();
    final String classname =
        this.getClass().getName().substring(1 + this.getClass().getName().lastIndexOf("."));
    b.append(classname + "[Xar=").append(this.getFileName());
    b.append(", libId=").append((this.libraryId == null ? "false" : this.libraryId.toString()));
    b.append(", instr=").append(this.instrument);
    b.append(", instrCtx=")
        .append(
            (JarAnalyzer.getAppContext() == null
                ? "false"
                : JarAnalyzer.getAppContext().toString(false)))
        .append("]");
    return b.toString();
  }

  /**
   * <p>getInstrumentationControl.</p>
   *
   * @return a {@link org.eclipse.steady.java.monitor.InstrumentationControl} object.
   */
  public InstrumentationControl getInstrumentationControl() {
    return this.instrControl;
  }

  /**
   * {@inheritDoc}
   *
   * In case the archive is rewritten, this method is used to rewrite certain {@link JarEntry}s
   * (rather than taking the file from the original archive).
   * The callback registration takes place in {@link #createInstrumentedArchive()}.
   */
  @Override
  public RewrittenJarEntry getInputStream(String _regex, JarEntry _entry) {
    InputStream is = null;
    long size = -1;
    long crc32 = -1;

    if (_regex.equals(".*.class$")) {
      JavaId jid = null;

      // Create JavaId from entry name
      try {
        String class_name = _entry.getName();
        class_name = class_name.substring(0, class_name.length() - 6); // ".class"
        class_name = class_name.replace('/', '.');
        jid = JavaId.parseClassQName(class_name);
      } catch (Exception e) {
        JarAnalyzer.log.error(
            "Cannot parse Java Id from Jar Entry [" + _entry.getName() + "]: " + e.getMessage());
        jid = null;
      }

      // Create input stream
      if (jid != null && this.instrumentedClasses.get(jid) != null) {
        final byte[] bytecode = this.instrumentedClasses.get(jid).getBytecode();
        crc32 = FileUtil.getCRC32(bytecode);
        size = bytecode.length;
        is = new ByteArrayInputStream(bytecode);
        return new RewrittenJarEntry(is, size, crc32);
      }
    }

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
    if (this.constructBodies == null) {
      this.constructBodies = new TreeMap<ConstructId, Construct>();
      for (ConstructId c : this.getConstructIds()) {
        this.constructBodies.put(c, new Construct(c, ""));
      }
    }
    return this.constructBodies;
  }

  /**
   * <p>getSharedConstructs.</p>
   *
   * @return a {@link java.util.List} object.
   * @throws org.eclipse.steady.FileAnalysisException if any.
   */
  public List<org.eclipse.steady.shared.json.model.ConstructId> getSharedConstructs()
      throws FileAnalysisException {
    List<org.eclipse.steady.shared.json.model.ConstructId> l =
        new ArrayList<org.eclipse.steady.shared.json.model.ConstructId>();
    for (ConstructId c : this.getConstructIds()) {
      l.add(
          new org.eclipse.steady.shared.json.model.ConstructId(
              ProgrammingLanguage.JAVA, c.getSharedType(), c.getQualifiedName()));
    }
    return l;
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

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    // We need to distinguish Jars with same digest but different path to be able to link parents to
    // their digest. However the relativePath should be used once we start using it.
    return obj instanceof JarAnalyzer
        && this.getSHA1().equals(((JarAnalyzer) obj).getSHA1())
        && this.getPath().toString().equals(((JarAnalyzer) obj).getPath().toString());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return this.getSHA1().hashCode();
  }

  // ---------------------------- STATIC METHODS

  /**
   * Returns true if the given name is a valid Java identifier, false otherwise.
   *
   * @param _name a {@link java.lang.String} object.
   * @return a boolean.
   */
  public static boolean isJavaIdentifier(String _name) {
    if (_name == null || _name.equals("")) return false;
    final char[] chars = _name.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (i == 0) {
        if (!Character.isJavaIdentifierStart(chars[i])) return false;
      } else {
        if (!Character.isJavaIdentifierPart(chars[i])) return false;
      }
    }
    return true;
  }

  /**
   * Returns the fully-qualified Java class identifier for a given JAR entry.
   * This is done by removing the file extension and by interpreting folders as
   * packages. package-info.class and module-info.class files are ignored, and
   * so are class files below META-INF/versions/ in multi-release JARs. In those
   * cases, and if anything else goes wrong, an IllegalArgumentException is
   * thrown.
   *
   * @param _jar_entry_name a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   * @throws IllegalArgumentException if the JAR entry name cannot be used for
   * constructing a valid fully-qualified Java class identifier
   */
  public static String getFqClassname(String _jar_entry_name) throws IllegalArgumentException {
    if (!_jar_entry_name.endsWith(".class")) {
      throw new IllegalArgumentException(
          "JAR entry [" + _jar_entry_name + "] does not have the file extension [class]");
    } else {
      // Class files below META-INF/versions
      if (_jar_entry_name.startsWith("META-INF/versions/")) {
        throw new IllegalArgumentException(
            "JAR entry ["
                + _jar_entry_name
                + "] corresponds to a class file in a multi-release JAR, which are not supported");
      }

      // package-info.class and module-info.class
      else if (_jar_entry_name.endsWith("package-info.class")
          || _jar_entry_name.endsWith("module-info.class")) {
        throw new IllegalArgumentException(
            "JAR entry ["
                + _jar_entry_name
                + "] corresponds to a package-info.class or module-info.class, which are ignored");
      }

      // Build class identifier for every other .class file
      else {
        final StringBuffer fqn = new StringBuffer();
        final Path p = Paths.get(_jar_entry_name);
        final Iterator<Path> i = p.iterator();

        // Loop over paths elements
        while (i.hasNext()) {
          String element = i.next().toString();
          if (element.endsWith(".class")) {
            element = element.substring(0, element.length() - 6); // ".class"
          }
          if (JarAnalyzer.isJavaIdentifier(element)) {
            if (fqn.length() != 0) {
              fqn.append('.');
            }
            fqn.append(element);
          } else {
            throw new IllegalArgumentException(
                "JAR entry ["
                    + _jar_entry_name
                    + "] cannot be transformed to a fully-qualified Java class identifier, because"
                    + " ["
                    + element
                    + "] is not a valid identifier");
          }
        }
        return fqn.toString();
      }
    }
  }

  /**
   * <p>setAppContext.</p>
   *
   * @param _ctx a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public static void setAppContext(Application _ctx) {
    JarAnalyzer.APP_CTX = _ctx;
  }
  /**
   * <p>getAppContext.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public static Application getAppContext() {
    return JarAnalyzer.APP_CTX;
  }

  /**
   * Adds a given URL to the classpath of the class pool. This allows maintaining dependencies needed for the compilation of instrumented classes.
   *
   * @param _url a {@link java.lang.String} object.
   * @throws javassist.NotFoundException
   */
  public static void insertClasspath(String _url) throws NotFoundException {
    CLASSPOOL.insertClassPath(_url);
  }
  /**
   * <p>getClassPool.</p>
   *
   * @return a {@link javassist.ClassPool} object.
   */
  protected static ClassPool getClassPool() {
    return JarAnalyzer.CLASSPOOL;
  }
}
