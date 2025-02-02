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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.steady.patcheval.representation;

import org.eclipse.steady.shared.enums.ConstructChangeType;

/**
 * Contains the results for a certain construct,path and library Id (i.e., for a line of the CSV file)
 */
public class ConstructPathLibResult2 {
  private String qname;
  private String path;
  private ConstructChangeType type;
  private LidResult2 lidResult;
  private String vulnAst;
  private String fixedAst;

  /**
   * <p>Constructor for ConstructPathLibResult2.</p>
   *
   * @param _qname a {@link java.lang.String} object.
   * @param _path a {@link java.lang.String} object.
   * @param constructChangeType a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   * @param _lidres a {@link org.eclipse.steady.patcheval.representation.LidResult2} object.
   * @param _v a {@link java.lang.String} object.
   * @param _f a {@link java.lang.String} object.
   */
  public ConstructPathLibResult2(
      String _qname,
      String _path,
      ConstructChangeType constructChangeType,
      LidResult2 _lidres,
      String _v,
      String _f) {
    this.qname = _qname;
    this.path = _path;
    this.type = constructChangeType;
    this.lidResult = _lidres;
    this.vulnAst = _v;
    this.fixedAst = _f;
  }

  /**
   * <p>Getter for the field <code>qname</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQname() {
    return qname;
  }

  /**
   * <p>Setter for the field <code>qname</code>.</p>
   *
   * @param qname a {@link java.lang.String} object.
   */
  public void setQname(String qname) {
    this.qname = qname;
  }

  /**
   * <p>Getter for the field <code>vulnAst</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVulnAst() {
    return vulnAst;
  }

  /**
   * <p>Setter for the field <code>vulnAst</code>.</p>
   *
   * @param vulnAst a {@link java.lang.String} object.
   */
  public void setVulnAst(String vulnAst) {
    this.vulnAst = vulnAst;
  }

  /**
   * <p>Getter for the field <code>fixedAst</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFixedAst() {
    return fixedAst;
  }

  /**
   * <p>Setter for the field <code>fixedAst</code>.</p>
   *
   * @param fixedAst a {@link java.lang.String} object.
   */
  public void setFixedAst(String fixedAst) {
    this.fixedAst = fixedAst;
  }

  /**
   * <p>Getter for the field <code>path</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPath() {
    return path;
  }

  /**
   * <p>Setter for the field <code>path</code>.</p>
   *
   * @param path a {@link java.lang.String} object.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * <p>Getter for the field <code>type</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getType() {
    return type;
  }

  /**
   * <p>Setter for the field <code>type</code>.</p>
   *
   * @param t a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   */
  public void setType(ConstructChangeType t) {
    this.type = t;
  }

  /**
   * <p>Getter for the field <code>lidResult</code>.</p>
   *
   * @return a {@link org.eclipse.steady.patcheval.representation.LidResult2} object.
   */
  public LidResult2 getLidResult() {
    return lidResult;
  }

  /**
   * <p>Setter for the field <code>lidResult</code>.</p>
   *
   * @param lidResult a {@link org.eclipse.steady.patcheval.representation.LidResult2} object.
   */
  public void setLidResult(LidResult2 lidResult) {
    this.lidResult = lidResult;
  }

  //    @Override
  //    public String toString(){
  //        StringBuilder sb = new StringBuilder();
  //        sb.append("Qname: ").append(qname).append("; path: ").append(path).append("; lidResults:
  // ");
  //        for ( LidResult lr : lidResults) {
  //            sb.append("\n");
  //            sb.append(lr.toString()).append(" ,");
  //        }
  //        return sb.toString();
  //    }
}
