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
package org.eclipse.steady.backend.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>ConstructId class.</p>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(
    name = "ConstructId",
    uniqueConstraints = @UniqueConstraint(columnNames = {"lang", "type", "qname"}),
    indexes = {
      @Index(name = "cid_lang_index", columnList = "lang"),
      @Index(name = "cid_type_index", columnList = "type"),
      @Index(name = "cid_qname_index", columnList = "qname"),
      @Index(name = "cid_index", columnList = "lang, type, qname")
    })
public class ConstructId implements Serializable, Comparable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @Column(nullable = false, length = 4)
  @Enumerated(EnumType.STRING)
  private ProgrammingLanguage lang;

  @Column(nullable = false, length = 4)
  @Enumerated(EnumType.STRING)
  private ConstructType type;

  @Column(nullable = false, length = 3072)
  private String qname;

  /**
   * <p>Constructor for ConstructId.</p>
   */
  public ConstructId() {
    super();
  }

  /**
   * <p>Constructor for ConstructId.</p>
   *
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @param type a {@link org.eclipse.steady.shared.enums.ConstructType} object.
   * @param qname a {@link java.lang.String} object.
   */
  public ConstructId(ProgrammingLanguage lang, ConstructType type, String qname) {
    super();
    this.lang = lang;
    this.type = type;
    this.qname = qname;
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * <p>Setter for the field <code>id</code>.</p>
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * <p>Getter for the field <code>lang</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   */
  public ProgrammingLanguage getLang() {
    return lang;
  }
  /**
   * <p>Setter for the field <code>lang</code>.</p>
   *
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   */
  public void setLang(ProgrammingLanguage lang) {
    this.lang = lang;
  }

  /**
   * <p>Getter for the field <code>type</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ConstructType} object.
   */
  public ConstructType getType() {
    return type;
  }
  /**
   * <p>Setter for the field <code>type</code>.</p>
   *
   * @param type a {@link org.eclipse.steady.shared.enums.ConstructType} object.
   */
  public void setType(ConstructType type) {
    this.type = type;
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
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder
        .append("[")
        .append(this.getLang())
        .append(":")
        .append(this.getType())
        .append(":")
        .append(this.getQname())
        .append("]");
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((lang == null) ? 0 : lang.hashCode());
    result = prime * result + ((qname == null) ? 0 : qname.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructId other = (ConstructId) obj;
    if (lang == null) {
      if (other.lang != null) return false;
    } else if (!lang.equals(other.lang)) return false;
    if (qname == null) {
      if (other.qname != null) return false;
    } else if (!qname.equals(other.qname)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Object _other) {
    if (_other == null || !(_other instanceof ConstructId)) throw new IllegalArgumentException();
    return this.getQname().compareTo(((ConstructId) _other).getQname());
  }

  /**
   * Returns a {@link ConstructId} of type {@link ConstructType#PACK} in which the given construct ID has been declared.
   *
   * @param _cid a {@link org.eclipse.steady.backend.model.ConstructId} object.
   * @return a {@link org.eclipse.steady.backend.model.ConstructId} object.
   */
  public static ConstructId getPackageOf(ConstructId _cid) {
    ConstructId pid = null;

    if (_cid.getType() == ConstructType.PACK) pid = _cid;
    else if (_cid.getType() == ConstructType.CLAS
        || _cid.getType() == ConstructType.ENUM
        || _cid.getType() == ConstructType.INTF
        || _cid.getType() == ConstructType.MODU) {
      final int idx = _cid.getQname().lastIndexOf(".");
      if (idx == -1) pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
      else
        pid =
            new ConstructId(_cid.getLang(), ConstructType.PACK, _cid.getQname().substring(0, idx));
    } else if (_cid.getType() == ConstructType.INIT
        || _cid.getType() == ConstructType.METH
        || (_cid.getType() == ConstructType.CONS && _cid.getLang() == ProgrammingLanguage.PY)) {
      int idx = _cid.getQname().lastIndexOf(".");
      final String ctx = _cid.getQname().substring(0, idx);
      idx = ctx.lastIndexOf(".");
      if (idx == -1) pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
      else pid = new ConstructId(_cid.getLang(), ConstructType.PACK, ctx.substring(0, idx));
    } else if (_cid.getType() == ConstructType.FUNC) {
      int idx = _cid.getQname().lastIndexOf(".");
      String ctx = _cid.getQname().substring(0, idx);
      while (ctx.contains("(") && ctx.lastIndexOf(".") != -1) {
        ctx = ctx.substring(0, ctx.lastIndexOf("."));
      }
      if (idx == -1) pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
      else pid = new ConstructId(_cid.getLang(), ConstructType.PACK, ctx);
    } else if (_cid.getType() == ConstructType.CONS && _cid.getLang() == ProgrammingLanguage.JAVA) {
      final int idx = _cid.getQname().lastIndexOf(".");
      if (idx == -1) pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
      else
        pid =
            new ConstructId(_cid.getLang(), ConstructType.PACK, _cid.getQname().substring(0, idx));
    }
    return pid;
  }

  /**
   * <p>toSharedType.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public org.eclipse.steady.shared.json.model.ConstructId toSharedType() {
    final org.eclipse.steady.shared.json.model.ConstructId shared_type =
        new org.eclipse.steady.shared.json.model.ConstructId();
    shared_type.setLang(this.getLang());
    shared_type.setType(this.getType());
    shared_type.setQname(this.getQname());
    return shared_type;
  }
}
