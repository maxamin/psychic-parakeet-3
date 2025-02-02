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
package org.eclipse.steady.java.bytecode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalContext;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.sign.ASTConstructBodySignature;
import org.eclipse.steady.java.sign.ASTSignatureChange;
import org.eclipse.steady.java.sign.gson.ASTSignatureChangeDeserializer;
import org.eclipse.steady.shared.enums.AffectedVersionSource;
import org.eclipse.steady.shared.enums.ConstructChangeType;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.JsonBuilder;
import org.eclipse.steady.shared.json.model.AffectedLibrary;
import org.eclipse.steady.shared.json.model.Bug;
import org.eclipse.steady.shared.json.model.ConstructChange;
import org.eclipse.steady.shared.json.model.Library;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.eclipse.steady.sign.SignatureFactory;

/**
 * <p>BytecodeComparator class.</p>
 */
public class BytecodeComparator {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(BytecodeComparator.class);

  private GoalContext context;
  private Map<Class<?>, StdDeserializer<?>> custom_deserializers =
      new HashMap<Class<?>, StdDeserializer<?>>();

  /**
   * <p>Constructor for BytecodeComparator.</p>
   */
  public BytecodeComparator() {
    this(null);
  }

  /**
   * <p>Constructor for BytecodeComparator.</p>
   *
   * @param _g a {@link org.eclipse.steady.goals.GoalContext} object
   */
  public BytecodeComparator(GoalContext _g) {
    custom_deserializers.put(ASTSignatureChange.class, new ASTSignatureChangeDeserializer());
    context = _g;
  }

  /**
   * <p>compareLibForBug.</p>
   *
   * @param _l a {@link org.eclipse.steady.shared.json.model.Library} object
   * @param _bug_id a {@link java.lang.String} object
   * @param _p a {@link java.nio.file.Path} object
   * @throws org.eclipse.steady.backend.BackendConnectionException if any.
   * @throws java.io.IOException if any.
   */
  public void compareLibForBug(Library _l, String _bug_id, Path _p)
      throws BackendConnectionException, IOException {
    boolean vuln = false;
    boolean fixed = false;

    Set<LibraryId> list = new HashSet<LibraryId>();

    final Bug b1 = BackendConnector.getInstance().getBug(context, _bug_id);

    // Retrieve existing affectedVersions
    final List<AffectedLibrary> alist = new ArrayList<AffectedLibrary>();
    for (AffectedVersionSource s : AffectedVersionSource.values()) {
      if (!s.equals(AffectedVersionSource.TO_REVIEW)) {
        AffectedLibrary[] als =
            BackendConnector.getInstance()
                .getBugAffectedLibraries(context, _bug_id, s.toString(), true);
        alist.addAll(Arrays.asList(als));
        log.debug(
            "Existing ["
                + als.length
                + "] affected libraries in backend for source ["
                + s.toString()
                + "]");
      }
    }

    // Check if current pair bug/digest was already assessed
    for (AffectedLibrary a : alist) {
      if (a.getLib() != null && a.getLib().getDigest().equals(_l.getDigest())) {
        return;
      }
    }

    b1.setAffectedVersions(alist);

    try {
      final JarFile archive = new JarFile(_p.toFile());
      for (ConstructChange cc : b1.getConstructChanges()) {

        // Only compare for type MOD of METH,CONS
        if (cc.getConstructChangeType().equals(ConstructChangeType.MOD)
            && (cc.getConstructId().getType().equals(ConstructType.CONS)
                || cc.getConstructId().getType().equals(ConstructType.METH))) {

          // Retrieve the construct from JAR
          JavaId jid =
              JavaId.getJavaId(
                  cc.getConstructId().getType().toString(), cc.getConstructId().getQname());
          JavaId def_ctx = JavaId.getCompilationUnit(jid);

          // Extract class file to disk
          Path classfile = null;
          final String entry_name = def_ctx.getQualifiedName().replace('.', '/') + ".class";
          final JarEntry entry = (JarEntry) archive.getEntry(entry_name);
          if (entry != null) {
            classfile =
                File.createTempFile(
                        def_ctx.getQualifiedName(),
                        ".class",
                        this.context.getVulasConfiguration().getTmpDir().toFile())
                    .toPath();
            log.debug("Extract class file to [" + classfile.toAbsolutePath() + "]");
            try (final InputStream ais = archive.getInputStream(entry);
                final FileOutputStream fos = new FileOutputStream(classfile.toFile()); ) {
              IOUtils.copy(ais, fos);
            }
          } else {
            log.warn(
                "Artifact does not contain entry ["
                    + entry_name
                    + "] for class ["
                    + def_ctx.getQualifiedName()
                    + "]");
          }

          // Create AST of construct to assess
          String ast_current = null;
          ASTConstructBodySignature sign = null;
          if (classfile != null) {
            SignatureFactory sf = CoreConfiguration.getSignatureFactory(JavaId.toSharedType(jid));
            sign =
                (ASTConstructBodySignature)
                    sf.createSignature(JavaId.toSharedType(jid), classfile.toFile());
            if (sign != null) ast_current = sign.toJson();
          }

          if (ast_current != null) {

            final ConstructBytecodeASTManager ast_mgr =
                new ConstructBytecodeASTManager(
                    this.context,
                    cc.getConstructId().getQname(),
                    cc.getRepoPath(),
                    cc.getConstructId().getType());
            for (AffectedLibrary a : b1.getAffectedVersions()) {
              if (a.getAffected() && a.getLibraryId() != null) ast_mgr.addVulnLid(a.getLibraryId());
              else if (!a.getAffected() && a.getLibraryId() != null)
                ast_mgr.addFixedLid(a.getLibraryId());
            }

            // Retrieve and compare source whose libid was assessed as vuln
            for (LibraryId v : ast_mgr.getVulnLids()) {
              log.debug(v.toString());
              // retrieve bytecode of the known to be vulnerable library
              String ast_lid = ast_mgr.getVulnAst(v);

              if (ast_lid != null) {
                // check if the ast's diff is empty

                String body = "[" + ast_lid + "," + ast_current + "]";
                String editV = BackendConnector.getInstance().getAstDiff(context, body);
                ASTSignatureChange scV =
                    (ASTSignatureChange)
                        JacksonUtil.asObject(editV, custom_deserializers, ASTSignatureChange.class);

                // SP check if scV get mofidications is null?
                log.debug(
                    "size to vulnerable lib "
                        + v.toString()
                        + " is ["
                        + scV.getModifications().size()
                        + "]");
                if (scV.getModifications().size() == 0) {

                  // check that there isn't also a construct = to vuln

                  log.info(
                      "Library ID equal to vuln based on AST bytecode comparison with "
                          + v.toString());
                  vuln = true;
                  list.add(v);
                  break;
                }
              }
            }

            // Retrieve and compare source whose libid was assessed as fixed
            for (LibraryId f : ast_mgr.getFixedLids()) {
              log.debug(f.toString());
              // retrieve bytecode of the known to be vulnerable library
              String ast_lid = ast_mgr.getFixedAst(f);

              if (ast_lid != null) {
                // check if the ast's diff is empty

                String body = "[" + ast_lid + "," + ast_current + "]";
                String editV = BackendConnector.getInstance().getAstDiff(context, body);
                ASTSignatureChange scV =
                    (ASTSignatureChange)
                        JacksonUtil.asObject(editV, custom_deserializers, ASTSignatureChange.class);

                log.debug(
                    "size to fixed lib "
                        + f.toString()
                        + " is ["
                        + scV.getModifications().size()
                        + "]");
                if (scV.getModifications().size() == 0) {

                  log.info(
                      "Library ID equal to fix based on AST bytecode comparison with "
                          + f.toString());
                  // cpa2.addLibsSameBytecode(l);
                  fixed = true;
                  list.add(f);
                  break;
                }
              }
            }

            if (vuln && fixed) {
              log.warn(
                  "No conclusion taken for vulnerability ["
                      + _bug_id
                      + "] in archive ["
                      + _l.getDigest()
                      + "]: Construct of change "
                      + cc.toString()
                      + " is equal both to a vulnerable and to a fixed archive");
              break;
            }
          }
        }

        // cia does not serve code for type class

        //		if(cc.getConstructChangeType().equals(ConstructChangeType.MOD) &&
        // cc.getConstructId().getType().equals(ConstructType.CLAS)) {
        //			// retrieve the bytecode of the currently analyzed library
        //			String cls_current = BackendConnector.getInstance().getSourcesForQnameInLib(context,
        // toAssess.getMvnGroup()+"/"+toAssess.getArtifact()
        //
        //	+"/"+toAssess.getVersion()+"/"+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname());
        //
        //			if(cls_current!=null){
        //				for(AffectedLibrary[] array: existingxSource.values()){
        //					for(AffectedLibrary a : array){
        //						if(a.getLibraryId()!=null) {
        //							String cls_known = BackendConnector.getInstance().getSourcesForQnameInLib(context,
        // a.getLibraryId().getMvnGroup()+"/"+a.getLibraryId().getArtifact()
        //
        //	+"/"+a.getLibraryId().getVersion()+"/"+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname());
        //
        //							if(cls_known!=null && cls_current.equals(cls_known)) {
        //								list.add(a.getLibraryId());
        //								if(a.getAffected())
        //									vuln=true;
        //								else if(!a.getAffected())
        //									fixed=true;
        //							}
        //						}
        //					}
        //				}
        //			}
        //		}

      }
      archive.close();
    } catch (ZipException ze) {
      log.error("Error in opening archive [" + _p + "]: " + ze.getMessage(), ze);
    }

    // Only create assessment if all constructs are equal to either vulnerable or fixed
    if (vuln ^ fixed) {
      log.info(
          "Library with digest ["
              + _l.getDigest()
              + "] assessed as ["
              + (vuln ? "vulnerable" : "non-vulnerable")
              + "] with regard to bug ["
              + b1.getBugId()
              + "]");

      final AffectedLibrary al = new AffectedLibrary();
      al.setBugId(b1);
      al.setLib(_l);
      al.setAffected(vuln);
      al.setExplanation("Same bytecode found in library(ies) [" + list.toString() + "]");
      al.setSource(AffectedVersionSource.CHECK_CODE);

      final JsonBuilder json = new JsonBuilder().startArray();
      json.appendJsonToArray(JacksonUtil.asJsonString(al));
      json.endArray();

      BackendConnector.getInstance()
          .uploadBugAffectedLibraries(
              context, b1.getBugId(), json.getJson(), AffectedVersionSource.CHECK_CODE);
    }

    // Some constructs are equal to vulnerable, others are equal to fixed
    else if (vuln && fixed) {
      log.warn(
          "No conclusion taken for bug ["
              + _bug_id
              + "] in archive ["
              + _l.getDigest()
              + "]: found equalities both to vulnerable and fixed archive");
    }

    // No conclusion taken
    else {
      log.warn("No conclusion taken for bug [" + _bug_id + "] in archive [" + _l.getDigest() + "]");
    }
  }
}
