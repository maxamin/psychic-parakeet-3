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
package org.eclipse.steady.cia.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.FileAnalyzerFactory;
import org.eclipse.steady.cia.util.ClassDownloader;
import org.eclipse.steady.cia.util.FileAnalyzerFetcher;
import org.eclipse.steady.cia.util.RepoException;
import org.eclipse.steady.cia.util.RepositoryDispatcher;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.sign.ASTConstructBodySignature;
import org.eclipse.steady.java.sign.ASTSignatureComparator;
import org.eclipse.steady.python.PythonId;
import org.eclipse.steady.python.sign.PythonConstructDigest;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.sign.Signature;
import org.eclipse.steady.sign.SignatureChange;
import org.eclipse.steady.sign.SignatureComparator;
import org.eclipse.steady.sign.SignatureFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;

/**
 * <p>ConstructController class.</p>
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/constructs")
public class ConstructController {

  private static Logger log = LoggerFactory.getLogger(ConstructController.class);

  /**
   * Provided that (1) the given GAV is known to the software repo (e.g., Maven Central) and (2) an artifact with classifier 'source' exists for it,
   * the method returns the source code of the given Java construct.
   * Attention: For the time being, only the types CONSTRUCTOR and METHOD are supported.
   *
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @param qname a {@link java.lang.String} object.
   * @param response a {@link javax.servlet.http.HttpServletResponse} object.
   */
  @RequestMapping(
      value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/{type:.+}/{qname:.+}",
      method = RequestMethod.GET)
  public void getConstructSourceForGav(
      @PathVariable String mvnGroup,
      @PathVariable String artifact,
      @PathVariable String version,
      @PathVariable String type,
      @PathVariable String qname,
      HttpServletResponse response) {
    Path file = null;
    JavaId jid = null;
    JavaId def_ctx = null;
    try {
      jid = JavaId.getJavaId(type, qname);
      def_ctx = JavaId.getCompilationUnit(jid);
      log.debug("Determined compilation unit " + def_ctx + " for qname [" + qname + "]");
      file =
          ClassDownloader.getInstance()
              .getClass(
                  mvnGroup,
                  artifact,
                  version,
                  def_ctx.getQualifiedName(),
                  ClassDownloader.Format.JAVA);

      if (file == null) {
        response.sendError(
            HttpServletResponse.SC_NOT_FOUND,
            "Cannot retrieve class [" + def_ctx.getQualifiedName() + "]");
        response.flushBuffer();
      } else {
        // Use ANTLR to parse the Java file
        FileAnalyzer jfa = FileAnalyzerFactory.buildFileAnalyzer(file.toFile());
        if (jfa.containsConstruct(jid)) {
          // Headers
          response.setContentType("text/plain");
          final String source_code = jfa.getConstruct(jid).getContent();
          response.getWriter().print(source_code);
          response.flushBuffer();
        } else {
          response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find construct " + jid);
          response.flushBuffer();
        }

        // Delete
        try {
          Files.delete(file);
        } catch (Exception e) {
          log.error("Cannot delete file [" + file + "]: " + e.getMessage());
        }
      }
    } catch (IllegalArgumentException iae) {
      log.error("Error: " + iae.getMessage(), iae);
      throw new RuntimeException(iae.getMessage());
    } catch (FileNotFoundException e) {
      log.error("Error: " + e.getMessage(), e);
      throw new RuntimeException("Cannot read file [" + file + "]");
    } catch (IOException e) {
      log.error("Error: " + e.getMessage(), e);
      throw new RuntimeException("IO exception when reading file [" + file + "]");
    } catch (Exception e) {
      log.error("Error: " + e.getMessage(), e);
      throw new RuntimeException(
          e.getClass().getSimpleName() + " when writing file to output stream: " + e.getMessage());
    }
  }

  /**
   * Provided that (1) the given SHA1 is known to the software repo (e.g., Maven Central) and (2) an artifact with classifier 'source' exists for the corresponding GAV,
   * the method returns the source code of the given Java construct.
   * Attention: For the time being, only the types CONSTRUCTOR and METHOD are supported.
   *
   * @param sha1 a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @param qname a {@link java.lang.String} object.
   * @param response a {@link javax.servlet.http.HttpServletResponse} object.
   */
  @RequestMapping(value = "/{sha1:.+}/{type:.+}/{qname:.+}", method = RequestMethod.GET)
  public void getConstructSourceForSha1(
      @PathVariable String sha1,
      @PathVariable String type,
      @PathVariable String qname,
      HttpServletResponse response) {
    Path file = null;
    JavaId jid = null;
    JavaId def_ctx = null;

    try {
      RepositoryDispatcher r = new RepositoryDispatcher();
      final Artifact doc = r.getArtifactForDigest(sha1);
      if (doc == null) throw new RuntimeException("SHA1 [" + sha1 + "] not found");

      jid = JavaId.getJavaId(type, qname);
      def_ctx = JavaId.getCompilationUnit(jid);
      log.debug("Determined compilation unit " + def_ctx + " for qname [" + qname + "]");
      file =
          ClassDownloader.getInstance()
              .getClass(
                  doc.getLibId().getMvnGroup(),
                  doc.getLibId().getArtifact(),
                  doc.getLibId().getVersion(),
                  def_ctx.getQualifiedName(),
                  ClassDownloader.Format.JAVA);

      if (file == null) {
        response.sendError(
            HttpServletResponse.SC_NOT_FOUND,
            "Cannot retrieve class [" + def_ctx.getQualifiedName() + "]");
        response.flushBuffer();
      } else {
        // Use ANTLR to parse the Java file
        FileAnalyzer jfa = FileAnalyzerFactory.buildFileAnalyzer(file.toFile());
        if (jfa.containsConstruct(jid)) {
          // Headers
          response.setContentType("text/plain");
          final String source_code = jfa.getConstruct(jid).getContent();
          response.getWriter().print(source_code);
          response.flushBuffer();
        } else {
          response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find construct " + jid);
          response.flushBuffer();
        }

        // Delete
        try {
          Files.delete(file);
        } catch (Exception e) {
          log.error("Cannot delete file [" + file + "]: " + e.getMessage());
        }
      }
    } catch (IllegalArgumentException iae) {
      throw new RuntimeException(iae.getMessage());
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Cannot read file [" + file + "]");
    } catch (IOException e) {
      throw new RuntimeException("IO exception when reading file [" + file + "]");
    } catch (Exception e) {
      throw new RuntimeException("Exception writing file to output stream");
    }
  }

  //	/**
  //	 *
  //	 * @param _jid
  //	 * @return
  //	 */
  //	private JavaId getCompilationUnit(JavaId _jid) {
  //		// Got it --> return provided object
  //		if( (_jid.getType().equals(JavaId.Type.CLASS) && !((JavaClassId)_jid).isNestedClass()) ||
  //				(_jid.getType().equals(JavaId.Type.INTERFACE) && !((JavaInterfaceId)_jid).isNested()) ||
  //				(_jid.getType().equals(JavaId.Type.ENUM) && !((JavaEnumId)_jid).isNested()) ) {
  //			return _jid;
  //		} else {
  //			return this.getCompilationUnit((JavaId)_jid.getDefinitionContext());
  //		}
  //	}
  //
  //	private JavaId getJavaId(String _type, String _qname) {
  //		JavaId.Type type = JavaId.typeFromString(_type);
  //
  //		// Check params
  //		if(JavaId.Type.METHOD!=type && JavaId.Type.CONSTRUCTOR!=type)
  //			throw new IllegalArgumentException("Only types METH and CONS are supported, got [" + type +
  // "]");
  //
  //		// Parse JavaId
  //		JavaId jid = null;
  //		if(JavaId.Type.CONSTRUCTOR==type)
  //			jid = JavaId.parseConstructorQName(_qname);
  //		else if(JavaId.Type.METHOD==type)
  //			jid = JavaId.parseMethodQName(_qname);
  //
  //		return jid;
  //	}

  /**
   * Provided that (1) the given GAV is known to the software repo (e.g., Maven Central) and (2) an artifact with classifier 'source' exists for it,
   * the method returns the abstract syntax tree (AST) of the given Java construct.
   * Attention: For the time being, only the types CONSTRUCTOR and METHOD are supported.
   *
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @param qname a {@link java.lang.String} object.
   * @param sources a {@link java.lang.Boolean} object.
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/{type:.+}/{qname:.+}/sign",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<Signature> getConstructAstForGav(
      @PathVariable String mvnGroup,
      @PathVariable String artifact,
      @PathVariable String version,
      @PathVariable String type,
      @PathVariable String qname,
      @RequestParam(value = "sources", required = false, defaultValue = "true") Boolean sources,
      @RequestParam(value = "lang", required = true, defaultValue = "JAVA")
          ProgrammingLanguage lang) {
    Path file = null;
    Signature sign = null;

    if (lang == ProgrammingLanguage.JAVA) {
      JavaId jid = null;
      JavaId def_ctx = null;
      try {
        jid = JavaId.getJavaId(type, qname);
        def_ctx = JavaId.getCompilationUnit(jid);
        log.debug("Determined compilation unit " + def_ctx + " for qname [" + qname + "]");
        if (sources)
          file =
              ClassDownloader.getInstance()
                  .getClass(
                      mvnGroup,
                      artifact,
                      version,
                      def_ctx.getQualifiedName(),
                      ClassDownloader.Format.JAVA);
        else
          file =
              ClassDownloader.getInstance()
                  .getClass(
                      mvnGroup,
                      artifact,
                      version,
                      def_ctx.getQualifiedName(),
                      ClassDownloader.Format.CLASS);
        if (file == null) {
          log.warn("Cannot retrieve class [" + def_ctx.getQualifiedName() + "]");
          return new ResponseEntity<Signature>(HttpStatus.NOT_FOUND);
        } else {
          SignatureFactory sf = CoreConfiguration.getSignatureFactory(JavaId.toSharedType(jid));
          // TODO: modify the createSignature implementation in lang-java in order to create
          // signature considering the file type!
          sign =
              (ASTConstructBodySignature)
                  sf.createSignature(JavaId.toSharedType(jid), file.toFile());
          // Delete
          try {
            Files.delete(file);
          } catch (Exception e) {
            log.error("Cannot delete file [" + file + "]: " + e.getMessage());
          }

          if (sign == null) {
            log.warn("Cannot construct signature for class [" + def_ctx.getQualifiedName() + "]");
            return new ResponseEntity<Signature>(HttpStatus.NOT_FOUND);
          }
        }
      } catch (IllegalArgumentException iae) {
        log.error(iae.getMessage());
        return new ResponseEntity<Signature>(HttpStatus.BAD_REQUEST);
      } catch (Exception e) {
        log.error(
            "Exception writing file to output stream: "
                + e.getMessage()
                + ". Request was ast for qname ["
                + qname
                + "] of type ["
                + type
                + "] in gav ["
                + mvnGroup
                + ":"
                + artifact
                + ":"
                + version,
            e);
        return new ResponseEntity<Signature>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } else if (lang == ProgrammingLanguage.PY) {
      try {
        // check that the artifact exists (with the requested packaging)
        RepositoryDispatcher r = new RepositoryDispatcher();
        String pack = (sources) ? "sdist" : "bdist_wheel";
        Artifact response = r.getArtifactVersion(mvnGroup, artifact, version, null, pack, lang);

        if (response == null) return new ResponseEntity<Signature>(HttpStatus.NOT_FOUND);
        else {
          Artifact a = new Artifact(mvnGroup, artifact, version);
          a.setPackaging(pack);
          a.setProgrammingLanguage(lang);

          FileAnalyzer fa = FileAnalyzerFetcher.read(a);

          org.eclipse.steady.shared.json.model.ConstructId pid =
              new org.eclipse.steady.shared.json.model.ConstructId(
                  ProgrammingLanguage.PY,
                  PythonId.toSharedType(PythonId.typeFromString(type)),
                  qname);

          final Set<org.eclipse.steady.ConstructId> constructs_in = fa.getConstructs().keySet();
          for (org.eclipse.steady.ConstructId c : constructs_in) {
            String aaa = c.getQualifiedName();
            // log.info(PythonId.toSharedType(c).toString());
            // log.info("requested"+pid.toString());
            if (PythonId.toSharedType(c).equals(pid)) {
              SignatureFactory sf = CoreConfiguration.getSignatureFactory(pid);
              sign = (PythonConstructDigest) sf.createSignature(fa.getConstructs().get(c));
              break;
            }
          }
          if (sign == null) {
            log.warn("Cannot construct signature for python construct [" + pid + "]");
            return new ResponseEntity<Signature>(HttpStatus.NOT_FOUND);
          }
        }
      } catch (Exception e) {
        log.error(
            "Exception writing file to output stream: "
                + e.getMessage()
                + ". Request was ast for qname ["
                + qname
                + "] of type ["
                + type
                + "] in gav ["
                + mvnGroup
                + ":"
                + artifact
                + ":"
                + version,
            e);
        return new ResponseEntity<Signature>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return new ResponseEntity<Signature>(sign, HttpStatus.OK);
  }

  /**
   * Provided that (1) the given SHA1 is known to the software repo (e.g., Maven Central) and (2) an artifact with classifier 'source' exists for the corresponding GAV,
   * the method returns the abstract syntax tree (AST) of the given Java construct.
   * Attention: For the time being, only the types CONSTRUCTOR and METHOD are supported.
   *
   * @param sha1 a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @param qname a {@link java.lang.String} object.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/{sha1}/{type}/{qname}/ast",
      method = RequestMethod.GET,
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<ASTConstructBodySignature> getConstructAstForSha1(
      @PathVariable String sha1, @PathVariable String type, @PathVariable String qname) {
    try {
      RepositoryDispatcher r = new RepositoryDispatcher();
      final Artifact doc = r.getArtifactForDigest(sha1);
      if (doc == null) return new ResponseEntity<ASTConstructBodySignature>(HttpStatus.NOT_FOUND);

      Path file = null;
      JavaId jid = null;
      JavaId def_ctx = null;

      jid = JavaId.getJavaId(type, qname);
      def_ctx = JavaId.getCompilationUnit(jid);
      log.info("Determined compilation unit " + def_ctx + " for qname [" + qname + "]");

      file =
          ClassDownloader.getInstance()
              .getClass(
                  doc.getLibId().getMvnGroup(),
                  doc.getLibId().getArtifact(),
                  doc.getLibId().getVersion(),
                  def_ctx.getQualifiedName(),
                  ClassDownloader.Format.JAVA);

      if (file == null) {
        log.warn("Cannot retrieve class [" + def_ctx.getQualifiedName() + "]");
        return new ResponseEntity<ASTConstructBodySignature>(HttpStatus.NOT_FOUND);
      } else {
        SignatureFactory sf = CoreConfiguration.getSignatureFactory(JavaId.toSharedType(jid));
        ASTConstructBodySignature sign =
            (ASTConstructBodySignature) sf.createSignature(JavaId.toSharedType(jid), file.toFile());

        // Delete
        try {
          Files.delete(file);
        } catch (Exception e) {
          log.error("Cannot delete file [" + file + "]: " + e.getMessage());
        }

        if (sign != null) return new ResponseEntity<ASTConstructBodySignature>(sign, HttpStatus.OK);
        else {
          log.warn(
              "Cannot construct signature for class ["
                  + def_ctx.getQualifiedName()
                  + "] in SHA1: "
                  + sha1);
          return new ResponseEntity<ASTConstructBodySignature>(sign, HttpStatus.NOT_FOUND);
        }
      }
    } catch (RepoException e) {
      return new ResponseEntity<ASTConstructBodySignature>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (IllegalArgumentException iae) {
      log.error(iae.getMessage());
      return new ResponseEntity<ASTConstructBodySignature>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("Exception writing file to output stream: " + e.getMessage());
      return new ResponseEntity<ASTConstructBodySignature>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>diffConstructAsts.</p>
   *
   * @param asts an array of {@link org.eclipse.steady.java.sign.ASTConstructBodySignature} objects.
   * @return a {@link org.springframework.http.ResponseEntity} object.
   */
  @RequestMapping(
      value = "/diff",
      method = RequestMethod.POST,
      consumes = {"application/json;charset=UTF-8"},
      produces = {"application/json;charset=UTF-8"})
  public ResponseEntity<SignatureChange> diffConstructAsts(
      @RequestBody ASTConstructBodySignature[] asts) {
    // Check args
    if (asts == null || asts.length != 2) {
      log.error(
          "Exactly two ASTs are required as input, got ["
              + (asts == null ? null : asts.length)
              + "]");
      return new ResponseEntity<SignatureChange>(HttpStatus.BAD_REQUEST);
    }
    try {
      final SignatureComparator signComparator = new ASTSignatureComparator();
      SourceCodeEntity.setEditScriptRelax(true);
      final SignatureChange ddt = signComparator.computeChange(asts[0], asts[1]);

      if (ddt != null) {
        return new ResponseEntity<SignatureChange>(ddt, HttpStatus.OK);
      } else {
        return new ResponseEntity<SignatureChange>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      log.error(e.getClass().getSimpleName() + " when comparing two ASTs: " + e.getMessage(), e);
      log.error("AST #1:" + asts[0].toJson());
      log.error("AST #2:" + asts[1].toJson());
      return new ResponseEntity<SignatureChange>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
