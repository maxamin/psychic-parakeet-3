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
package org.eclipse.steady.shared.connectivity;

import javax.validation.constraints.NotNull;

import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Space;
import org.eclipse.steady.shared.json.model.Tenant;

/**
 * Builds URI paths matching to the RESTful API offered by the various {@link Service}s.
 */
public class PathBuilder {

  /**
   * <p>root.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String root() {
    return "/";
  }

  /**
   * <p>spaces.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String spaces() {
    final StringBuilder b = new StringBuilder();
    b.append(root()).append("spaces");
    return b.toString();
  }

  /**
   * <p>space.</p>
   *
   * @param _space a {@link org.eclipse.steady.shared.json.model.Space} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String space(@NotNull Space _space) {
    final StringBuilder b = new StringBuilder();
    b.append(spaces()).append("/");
    b.append(_space.getSpaceToken());
    return b.toString();
  }

  /**
   * <p>apps.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String apps() {
    final StringBuilder b = new StringBuilder();
    b.append(root()).append("apps");
    return b.toString();
  }

  /**
   * vulas-backend:/apps/{group}/{artifact}
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String artifact(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(apps()).append("/");
    b.append(_app.getMvnGroup()).append("/").append(_app.getArtifact());
    return b.toString();
  }

  /**
   * <p>app.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String app(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(apps()).append("/");
    b.append(_app.getMvnGroup())
        .append("/")
        .append(_app.getArtifact())
        .append("/")
        .append(_app.getVersion());
    return b.toString();
  }

  /**
   * <p>goalExcecutions.</p>
   *
   * @param _t a {@link org.eclipse.steady.shared.json.model.Tenant} object.
   * @param _s a {@link org.eclipse.steady.shared.json.model.Space} object.
   * @param _a a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String goalExcecutions(@NotNull Tenant _t, Space _s, Application _a) {
    final StringBuilder b = new StringBuilder();
    if (_a != null) b.append(app(_a)).append("/goals");
    else if (_s != null) b.append(space(_s)).append("/goals");
    return b.toString();
  }

  /**
   * <p>goalExcecution.</p>
   *
   * @param _t a {@link org.eclipse.steady.shared.json.model.Tenant} object.
   * @param _s a {@link org.eclipse.steady.shared.json.model.Space} object.
   * @param _a a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @param _gexe_id a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String goalExcecution(
      @NotNull Tenant _t, Space _s, Application _a, String _gexe_id) {
    final StringBuilder b = new StringBuilder();
    if (_a != null) b.append(app(_a)).append("/goals");
    else if (_s != null) b.append(space(_s)).append("/goals");
    b.append("/").append(_gexe_id);
    return b.toString();
  }

  /**
   * <p>appConstructIds.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appConstructIds(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/constructIds");
    return b.toString();
  }

  /**
   * <p>appTraces.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appTraces(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/traces");
    return b.toString();
  }

  /**
   * <p>appReachableConstructIds.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appReachableConstructIds(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/reachableConstructIds");
    return b.toString();
  }

  /**
   * <p>appBugs.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appBugs(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/bugs");
    return b.toString();
  }

  /**
   * vulas-backend:/apps/{group}/{artifact}/{version}/deps
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appDeps(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/deps");
    return b.toString();
  }

  /**
   * vulas-backend:/apps/{group}/{artifact}/{version}/vulndeps
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @param _include_historical a boolean.
   * @param _include_affected a boolean.
   * @param _include_affected_unconfirmed a boolean.
   * @return a {@link java.lang.String} object.
   */
  public static final String appVulnDeps(
      @NotNull Application _app,
      boolean _include_historical,
      boolean _include_affected,
      boolean _include_affected_unconfirmed) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/vulndeps");
    b.append("?").append("includeHistorical=").append(_include_historical);
    b.append("&").append("includeAffected=").append(_include_affected);
    b.append("&").append("includeAffectedUnconfirmed=").append(_include_affected_unconfirmed);
    return b.toString();
  }

  /**
   * <p>vulnDeps.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String vulnDeps() {
    final StringBuilder b = new StringBuilder();
    b.append("/apps/vulndeps");
    return b.toString();
  }

  /**
   * <p>appPaths.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appPaths(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/paths");
    return b.toString();
  }

  /**
   * <p>libs.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String libs() {
    final StringBuilder b = new StringBuilder();
    b.append(root()).append("libs");
    return b.toString();
  }

  /**
   * <p>lib.</p>
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String lib(String _sha1) {
    final StringBuilder b = new StringBuilder();
    b.append(libs()).append("/").append(_sha1);
    return b.toString();
  }

  /**
   * <p>libupload.</p>
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String libupload(String _sha1) {
    final StringBuilder b = new StringBuilder();
    b.append(libs()).append("/").append(_sha1).append("/upload");
    return b.toString();
  }

  /**
   * <p>libVersionCheck.</p>
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String libVersionCheck(String _sha1) {
    final StringBuilder b = new StringBuilder();
    b.append(lib(_sha1)).append("/versionCheck");
    return b.toString();
  }

  /**
   * <p>libbugs.</p>
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String libbugs(String _sha1) {
    final StringBuilder b = new StringBuilder();
    b.append(lib(_sha1)).append("/bugs");
    return b.toString();
  }

  /**
   * <p>bugs.</p>
   *
   * @param _l a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String bugs(ProgrammingLanguage _l) {
    final StringBuilder b = new StringBuilder();
    b.append(root()).append("bugs");
    if (_l != null) b.append("?lang=").append(_l);
    return b.toString();
  }

  /**
   * <p>bug.</p>
   *
   * @param _bugid a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String bug(String _bugid) {
    final StringBuilder b = new StringBuilder();
    b.append(bugs(null)).append("/").append(_bugid);
    return b.toString();
  }

  /**
   * <p>bugAffectedLibs.</p>
   *
   * @param _bugid a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String bugAffectedLibs(String _bugid) {
    final StringBuilder b = new StringBuilder();
    b.append(bug(_bugid)).append("/affectedLibIds");
    return b.toString();
  }

  /**
   * <p>appReachableConstructs.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appReachableConstructs(
      @NotNull Application _app, @NotNull String _sha1) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/deps/").append(_sha1).append("/reachableConstructIds");
    return b.toString();
  }

  /**
   * <p>appTouchPoints.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String appTouchPoints(@NotNull Application _app, @NotNull String _sha1) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/deps/").append(_sha1).append("/touchPoints");
    return b.toString();
  }

  // Formlery "xs/assessment/getVulnerableAppArchiveConstructs.xsjs"
  /**
   * <p>vulnArchiveConstructs.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String vulnArchiveConstructs(@NotNull Application _app) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/vulndeps");
    return b.toString();
  }

  /**
   * vulas-backend:/apps/{group}/{artifact}/{version}/vulndeps/{sha1}/bugs/{bugid}
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @param _sha1 a {@link java.lang.String} object.
   * @param _bugId a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String vulnerableDependencyConstructs(
      @NotNull Application _app, @NotNull String _sha1, @NotNull String _bugId) {
    final StringBuilder b = new StringBuilder();
    b.append(app(_app)).append("/vulndeps/").append(_sha1).append("/bugs/").append(_bugId);
    return b.toString();
  }

  //	public static final String vulnerableDependencyUpload(@NotNull String _bugId){
  //		final StringBuilder b = new StringBuilder();
  //		b.append("/bugs/").append(_bugId).append("/affectedLibIds");
  //		return b.toString();
  //	}

  /**
   * vulas-cia:/constructs
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String constructs() {
    final StringBuilder b = new StringBuilder();
    b.append(root()).append("constructs");
    return b.toString();
  }

  /**
   * vulas-cia:/constructs()/{sha1}/{type}/{qname}/ast
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @param _cid a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String constructSignature(
      @NotNull String _sha1, @NotNull org.eclipse.steady.shared.json.model.ConstructId _cid) {
    final StringBuilder b = new StringBuilder();
    b.append(constructs());
    b.append("/").append(_sha1);
    b.append("/").append(_cid.getType().toString());
    b.append("/").append(_cid.getQname());
    b.append("/ast");
    return b.toString();
  }

  /**
   * vulas-cia:/constructs()/{group}/{artifact}/{version}/{type}/{qname}/ast
   *
   * @param _lib a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @param _cid a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String constructSignature(
      @NotNull Application _lib, @NotNull org.eclipse.steady.shared.json.model.ConstructId _cid) {
    final StringBuilder b = new StringBuilder();
    b.append(constructs());
    b.append("/").append(_lib.getMvnGroup());
    b.append("/").append(_lib.getArtifact());
    b.append("/").append(_lib.getVersion());
    b.append("/").append(_cid.getType().toString());
    b.append("/").append(_cid.getQname());
    b.append("/ast");
    return b.toString();
  }

  /**
   * vulas-cia:/artifacts
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String artifacts() {
    final StringBuilder b = new StringBuilder();
    b.append(root()).append("artifacts");
    return b.toString();
  }

  /**
   * vulas-cia:/artifacts()/diff
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String diffArtifacts() {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts()).append("/").append("diff");
    return b.toString();
  }

  /**
   * vulas-cia:artifacts()/{group}/{artifact}/
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String artifactsGroupVersion(String _g, String _a) {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts()).append("/").append(_g).append("/").append(_a);
    return b.toString();
  }

  /**
   * vulas-cia:artifacts()/{group}/{artifact}/{version}
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String artifactsGAV(String _g, String _a, String _v) {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts()).append("/").append(_g).append("/").append(_a).append("/").append(_v);
    return b.toString();
  }

  /**
   * vulas-cia:artifacts()/{group}/{artifact}/latest
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String artifactsLatestGroupVersion(String _g, String _a) {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts()).append("/").append(_g).append("/").append(_a).append("/latest");
    return b.toString();
  }

  /**
   * vulas-cia:artifacts()/{group}/{artifact}/greaterThan/{version}
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param v a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String artifactsGreaterGroupVersion(String _g, String _a, String v) {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts())
        .append("/")
        .append(_g)
        .append("/")
        .append(_a)
        .append("/greaterThan/")
        .append(v);
    return b.toString();
  }

  /**
   * vulas-cia:artifacts()/{group}/{artifact}/{version}/constructIds
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String artifactsConstruct(String _g, String _a, String _v) {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts())
        .append("/")
        .append(_g)
        .append("/")
        .append(_a)
        .append("/")
        .append(_v)
        .append("/constructIds");
    return b.toString();
  }

  //	/**
  //	 * vulas-cia:artifacts()/{group}/{artifact}/status
  //	 * @return
  //	 */
  //	public static final String artifactStatus(String _g, String _a) {
  //		final StringBuilder b = new StringBuilder();
  //		b.append(artifacts()).append("/").append(_g).append("/").append(_a).append("/status");
  //		return b.toString();
  //	}

  //	/**
  //	 * vulas-cia:artifacts()/{group}/{artifact}/jars
  //	 * @return
  //	 */
  //	public static final String artifactJars(String _g, String _a) {
  //		final StringBuilder b = new StringBuilder();
  //		b.append(artifacts()).append("/").append(_g).append("/").append(_a).append("/jars");
  //		return b.toString();
  //	}

  /**
   * vulas-cia:artifacts()/{group}/{artifact}/{version}/jars/classifier=jar|sources
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @param _sources a {@link java.lang.Boolean} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String downloadArtifactJars(
      String _g, String _a, String _v, Boolean _sources) {
    final StringBuilder b = new StringBuilder();
    b.append(artifacts())
        .append("/")
        .append(_g)
        .append("/")
        .append(_a)
        .append("/")
        .append(_v)
        .append("/jar?classifier=");
    if (_sources) b.append("sources");
    return b.toString();
  }

  /**
   * vulas-backend:/bugs/{bugId}
   * @param _bugId
   * @return
   */
  /*	public static final String bugChangelist(@NotNull String _bugId){
  	final StringBuilder b = new StringBuilder();
  	b.append("/bugs/").append(_bugId);
  	return b.toString();
  }*/

  /**
   * vulas-backend:/bugs/{bugId}/libraries
   *
   * @param _bugId a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String bugLibraryVersions(@NotNull String _bugId) {
    final StringBuilder b = new StringBuilder();
    b.append("/bugs/").append(_bugId).append("/libraries");
    return b.toString();
  }

  /**
   * vulas-cia:/classes/libraryIds/{classname}
   *
   * @param _className a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String classesLibraryIds(@NotNull String _className) {
    final StringBuilder sb = new StringBuilder();
    sb.append("/classes/libraryIds/").append(_className);
    return sb.toString();
  }

  /**
   * vulas-cia:/constructs/{constructName}/ast
   *
   * @param qString a {@link java.lang.String} object.
   * @param _sources a {@link java.lang.Boolean} object.
   * @param _lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String astForQnameInLib(
      String qString, Boolean _sources, ProgrammingLanguage _lang) {
    final StringBuilder sb = new StringBuilder();
    sb.append("/constructs/")
        .append(qString)
        .append("/sign?sources=" + _sources)
        .append("&lang=" + _lang);
    return sb.toString();
  }

  /**
   * vulas-cia:/constructs/{constructName}
   *
   * @param qString a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String sourcesForQnameInLib(String qString) {
    final StringBuilder sb = new StringBuilder();
    sb.append("/constructs/").append(qString);
    return sb.toString();
  }

  /**
   * vulas-cia:/artifacts/{group}/{artifact}/{version}/constructIds/intersect
   *
   * @param qString a {@link java.lang.String} object.
   * @param packaging a {@link java.lang.String} object.
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String libConstructIdsIntersect(
      String qString, String packaging, ProgrammingLanguage lang) {
    final StringBuilder sb = new StringBuilder();
    // TODO to uncomment once we have cia for python
    sb.append("/artifacts/")
        .append(qString)
        .append("/")
        .append(packaging)
        .append("/constructIds/intersect?lang=")
        .append(lang);
    return sb.toString();
  }

  /**
   * vulas-cia:/constructs/diff
   *
   * @return a {@link java.lang.String} object.
   */
  public static final String constructsDiff() {
    final StringBuilder sb = new StringBuilder();
    sb.append("/constructs/diff");
    return sb.toString();
  }

  /**
   * Affected Lib Ids
   *
   * @param _bugId a {@link java.lang.String} object
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String affectedLibs(String _bugId, String _g, String _a, String _v) {
    final StringBuilder b = new StringBuilder();
    b.append(bugAffectedLibs(_bugId)).append("/").append(_g).append("/").append(_a);
    if (_v != null && !_v.isEmpty()) b.append("/").append(_v);
    return b.toString();
  }

  /**
   * backend:/cves/bug_id
   *
   * @param _bugId a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   */
  public static final String cve(@NotNull String _bugId) {
    final StringBuilder sb = new StringBuilder();
    sb.append("/cves/");
    sb.append(_bugId);
    return sb.toString();
  }
}
