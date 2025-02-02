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
package org.eclipse.steady.shared.json.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.steady.shared.enums.AffectedVersionSource;

/**
 * <p>VulnerableDependency class.</p>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VulnerableDependency implements Serializable, Comparable<VulnerableDependency> {

  private static final long serialVersionUID = 1L;

  private Dependency dep;

  private Bug bug;

  private List<ConstructChangeInDependency> constructList;

  @JsonProperty(value = "affected_version_confirmed")
  int affectedVersionConfirmed;

  @JsonProperty(value = "affected_version")
  int affectedVersion;

  AffectedVersionSource affectedVersionSource;

  int reachable;

  @JsonProperty(value = "reachable_confirmed")
  int reachableConfirmed;

  int traced;

  IExemption exemption = null;

  /**
   * <p>Constructor for VulnerableDependency.</p>
   */
  public VulnerableDependency() {
    super();
  }

  /**
   * <p>Constructor for VulnerableDependency.</p>
   *
   * @param d a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   * @param b a {@link org.eclipse.steady.shared.json.model.Bug} object.
   */
  public VulnerableDependency(Dependency d, Bug b) {
    super();
    this.dep = d;
    this.bug = b;
    this.evalAffectedVersion();
  }

  /**
   * <p>Getter for the field <code>dep</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   */
  public Dependency getDep() {
    return dep;
  }

  /**
   * <p>Setter for the field <code>dep</code>.</p>
   *
   * @param dep a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   */
  public void setDep(Dependency dep) {
    this.dep = dep;
  }

  /**
   * <p>getBugId.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getBugId() {
    return bug.getId();
  }

  /**
   * <p>Getter for the field <code>bug</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Bug} object.
   */
  public Bug getBug() {
    return bug;
  }

  /**
   * <p>Setter for the field <code>bug</code>.</p>
   *
   * @param bug a {@link org.eclipse.steady.shared.json.model.Bug} object.
   */
  public void setBug(Bug bug) {
    this.bug = bug;
  }

  /**
   * <p>Getter for the field <code>constructList</code>.</p>
   *
   * @return a {@link java.util.List} object.
   */
  public List<ConstructChangeInDependency> getConstructList() {
    return constructList;
  }

  /**
   * <p>Setter for the field <code>constructList</code>.</p>
   *
   * @param ccd a {@link java.util.List} object.
   */
  public void setConstructList(List<ConstructChangeInDependency> ccd) {
    this.constructList = ccd;
  }

  /**
   * <p>isAffectedVersion.</p>
   *
   * @return a boolean.
   */
  public boolean isAffectedVersion() {
    return this.getAffectedVersion() == 1;
  }

  /**
   * <p>evalAffectedVersion.</p>
   */
  public void evalAffectedVersion() {
    Boolean check_code = null, check_version = null, manual_libId = null, equal = null;
    AffectedVersionSource source = null;
    if (bug.getAffectedVersions() != null) {
      for (AffectedLibrary a : bug.getAffectedVersions()) {

        if (a.getLibraryId() != null
            && dep.getLib() != null
            && dep.getLib().getLibraryId() != null) {
          if (a.getLibraryId().equals(dep.getLib().getLibraryId())) {

            if (a.getSource() == AffectedVersionSource.MANUAL) {
              manual_libId = a.getAffected();
            } else if (a.getSource() == AffectedVersionSource.AST_EQUALITY
                || a.getSource() == AffectedVersionSource.MAJOR_EQUALITY
                || a.getSource() == AffectedVersionSource.MINOR_EQUALITY
                || a.getSource() == AffectedVersionSource.INTERSECTION
                || a.getSource() == AffectedVersionSource.GREATER_RELEASE) {
              equal = a.getAffected();
              source = a.getSource();
            } else if (a.getSource() == AffectedVersionSource.CHECK_CODE)
              check_code = a.getAffected();
          }
        } else if (a.getLib() != null && a.getLib() == dep.getLib()) {
          if (a.getSource() == AffectedVersionSource.MANUAL) {
            this.setAffectedVersion((a.getAffected()) ? 1 : 0);
            this.setAffectedVersionConfirmed(1);
            this.setAffectedVersionSource(AffectedVersionSource.MANUAL);
            return;
          } else if (a.getSource() == AffectedVersionSource.CHECK_VERSION)
            check_version = a.getAffected();
        } else if (a.getLib() != null && a.getLibraryId() != null) {
          System.out.println(
              "affectedLib with both LIB: " + a.getLib() + " , and LIBID: " + dep.getLib());
        }
      }
      if (manual_libId != null) {
        this.setAffectedVersion((manual_libId) ? 1 : 0);
        this.setAffectedVersionConfirmed(1);
        this.setAffectedVersionSource(AffectedVersionSource.MANUAL);
        return;
      }
      if (equal != null) {
        this.setAffectedVersion((equal) ? 1 : 0);
        this.setAffectedVersionConfirmed(1);
        this.setAffectedVersionSource(source);
        return;
      }
      if (check_version != null) {
        this.setAffectedVersion((check_version) ? 1 : 0);
        this.setAffectedVersionConfirmed(1);
        this.setAffectedVersionSource(AffectedVersionSource.CHECK_VERSION);
        return;
      }
      if (check_code != null) {
        this.setAffectedVersion((check_code) ? 1 : 0);
        this.setAffectedVersionConfirmed(1);
        this.setAffectedVersionSource(AffectedVersionSource.CHECK_CODE);
        return;
      }
    }
    this.setAffectedVersionConfirmed(0);
    this.setAffectedVersion(
        1); // when the confirmed flag is 0, the value of affected-version is irrelevant but we set
    // it to 1 so that the UI doesn't filter it out when filtering out historical
    // vulnerabilities
  }

  /**
   * <p>isAffectedVersionConfirmed.</p>
   *
   * @return a boolean.
   */
  public boolean isAffectedVersionConfirmed() {
    return this.getAffectedVersionConfirmed() == 1;
  }

  /**
   * <p>Getter for the field <code>affectedVersionConfirmed</code>.</p>
   *
   * @return a int.
   */
  public int getAffectedVersionConfirmed() {
    return this.affectedVersionConfirmed;
  }

  /**
   * <p>Setter for the field <code>affectedVersionConfirmed</code>.</p>
   *
   * @param i a int.
   */
  public void setAffectedVersionConfirmed(int i) {
    this.affectedVersionConfirmed = i;
  }

  /**
   * <p>Getter for the field <code>affectedVersion</code>.</p>
   *
   * @return a int.
   */
  public int getAffectedVersion() {
    return this.affectedVersion;
  }

  /**
   * <p>Setter for the field <code>affectedVersion</code>.</p>
   *
   * @param i a int.
   */
  public void setAffectedVersion(int i) {
    this.affectedVersion = i;
  }

  /**
   * <p>Getter for the field <code>affectedVersionSource</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   */
  public AffectedVersionSource getAffectedVersionSource() {
    return this.affectedVersionSource;
  }

  /**
   * <p>Setter for the field <code>affectedVersionSource</code>.</p>
   *
   * @param s a {@link org.eclipse.steady.shared.enums.AffectedVersionSource} object.
   */
  public void setAffectedVersionSource(AffectedVersionSource s) {
    this.affectedVersionSource = s;
  }

  /**
   * <p>isReachable.</p>
   *
   * @return a boolean.
   */
  public boolean isReachable() {
    return this.getReachable() == 1;
  }

  /**
   * <p>Getter for the field <code>reachable</code>.</p>
   *
   * @return a int.
   */
  public int getReachable() {
    return this.reachable;
  }

  /**
   * <p>Setter for the field <code>reachable</code>.</p>
   *
   * @param i a int.
   */
  public void setReachable(int i) {
    this.reachable = i;
  }

  /**
   * <p>isReachableConfirmed.</p>
   *
   * @return a boolean.
   */
  public boolean isReachableConfirmed() {
    return this.getReachableConfirmed() == 1;
  }

  /**
   * <p>Getter for the field <code>reachableConfirmed</code>.</p>
   *
   * @return a int.
   */
  public int getReachableConfirmed() {
    // Need to be calculated differently than in backend model, since the collection of reachable
    // constructs is not serialized
    // return (this.dep.getReachableConstructIds()==null ||
    // this.dep.getReachableConstructIds().size()==0) ? 0 : 1;
    return this.reachableConfirmed;
  }

  /**
   * <p>Setter for the field <code>reachableConfirmed</code>.</p>
   *
   * @param i a int.
   */
  public void setReachableConfirmed(int i) {
    this.reachableConfirmed = i;
  }

  /**
   * <p>isTraced.</p>
   *
   * @return a boolean.
   */
  public boolean isTraced() {
    return this.getTraced() == 1;
  }

  /**
   * <p>Getter for the field <code>traced</code>.</p>
   *
   * @return a int.
   */
  public int getTraced() {
    return this.traced;
  }

  /**
   * <p>Setter for the field <code>traced</code>.</p>
   *
   * @param i a int.
   */
  public void setTraced(int i) {
    this.traced = i;
  }

  /**
   * <p>isTracedConfirmed.</p>
   *
   * @return a boolean.
   */
  public boolean isTracedConfirmed() {
    return this.getTracedConfirmed() == 1;
  }

  /**
   * <p>getTracedConfirmed.</p>
   *
   * @return a int.
   */
  @JsonProperty(value = "traced_confirmed")
  public int getTracedConfirmed() {
    return this.dep.getTraced() != null && this.dep.getTraced() ? 1 : 0;
  }

  /**
   * <p>Getter for the field <code>exemption</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.IExemption} object.
   */
  public IExemption getExemption() {
    return this.exemption;
  }

  /**
   * <p>Setter for the field <code>exemption</code>.</p>
   *
   * @param _e a {@link org.eclipse.steady.shared.json.model.IExemption} object.
   */
  public void setExemption(IExemption _e) {
    this.exemption = _e;
  }

  /**
   * Returns true if the an exemption has been set before, false otherwise.
   *
   * @return a {@link java.lang.Boolean} object
   */
  public Boolean isExempted() {
    return this.exemption != null;
  }

  // =================== Added on top of the original class from vulas-backend

  /**
   * <p>isNotReachable.</p>
   *
   * @return a boolean.
   */
  public boolean isNotReachable() {
    return !isReachable() && isReachableConfirmed();
  }

  /**
   * <p>isNotTraced.</p>
   *
   * @return a boolean.
   */
  public boolean isNotTraced() {
    return !isTraced() && isTracedConfirmed();
  }

  /**
   * <p>isNoneAffectedVersion.</p>
   *
   * @return a boolean.
   */
  public boolean isNoneAffectedVersion() {
    return this.getAffectedVersion() == 0 && this.getAffectedVersionConfirmed() == 1;
  }

  private Boolean above_threshold;

  /**
   * <p>setAboveThreshold.</p>
   *
   * @param _b a boolean.
   */
  public void setAboveThreshold(boolean _b) {
    this.above_threshold = Boolean.valueOf(_b);
  }

  /**
   * <p>isAboveThreshold.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isAboveThreshold() {
    return above_threshold;
  }

  /**
   * <p>isThrowsException.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isThrowsException() {
    return this.isAboveThreshold() && !this.isExempted();
  }

  /**
   * <p>isThrowsExceptionExcluded.</p>
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isThrowsExceptionExcluded() {
    return this.isAboveThreshold() && this.isExempted();
  }

  public Application app = null;

  /**
   * <p>Getter for the field <code>app</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public Application getApp() {
    return this.app;
  }

  /**
   * <p>Setter for the field <code>app</code>.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public void setApp(Application _app) {
    this.app = _app;
  }

  /**
   * <p>getAffectedVersionSourceAsString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAffectedVersionSourceAsString() {
    if (this.affectedVersionSource == null) return "";
    else if (AffectedVersionSource.MANUAL.equals(this.affectedVersionSource)) return "M";
    else if (AffectedVersionSource.CHECK_CODE.equals(this.affectedVersionSource)) return "BC";
    else if (AffectedVersionSource.CHECK_VERSION.equals(this.affectedVersionSource)) return "C";
    else if (AffectedVersionSource.AST_EQUALITY.equals(this.affectedVersionSource)) return "E";
    else if (AffectedVersionSource.MAJOR_EQUALITY.equals(this.affectedVersionSource)) return "ME";
    else if (AffectedVersionSource.MINOR_EQUALITY.equals(this.affectedVersionSource)) return "mE";
    else if (AffectedVersionSource.INTERSECTION.equals(this.affectedVersionSource)) return "IE";
    else if (AffectedVersionSource.GREATER_RELEASE.equals(this.affectedVersionSource)) return "GE";
    else return String.valueOf(this.affectedVersionSource);
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(VulnerableDependency _o) {
    VulnerableDependency other = null;
    if (_o instanceof VulnerableDependency) other = (VulnerableDependency) _o;
    else throw new IllegalArgumentException();

    final int filename_comparison =
        this.getDep().getFilename().compareTo(other.getDep().getFilename());
    final int bugid_comparison = this.getBug().getBugId().compareTo(other.getBug().getBugId());

    if (filename_comparison != 0) return filename_comparison;
    else return bugid_comparison;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((bug == null) ? 0 : bug.hashCode());
    result = prime * result + ((dep == null) ? 0 : dep.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    VulnerableDependency other = (VulnerableDependency) obj;
    if (app == null) {
      if (other.app != null) return false;
    } else if (!app.equals(other.app)) return false;
    if (bug == null) {
      if (other.bug != null) return false;
    } else if (!bug.equals(other.bug)) return false;
    if (dep == null) {
      if (other.dep != null) return false;
    } else if (!dep.equals(other.dep)) return false;
    return true;
  }
}
