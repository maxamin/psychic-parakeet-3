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
package org.eclipse.steady.shared.json;

/**
 * Home-grown JSON builder.
 */
public class JsonBuilder {
  private final StringBuffer b = new StringBuffer();
  private int openArrays = 0;
  private int openObjects = 0;

  /** Returns the last character of the given StringBuffer.
   * @param _buffer
   * @return the last character
   */
  private char getLast() {
    return b.charAt(b.length() - 1);
  }

  /**
   * Supports appending array elements and object properties by adding a comma to the buffer unless the last char is equal to [ or { (i.e., the beginning of an array or object).
   */
  private void fixComma() {
    if (this.b.length() > 0 && this.getLast() != '[' && this.getLast() != '{') b.append(",");
  }

  /**
   * <p>startArray.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder startArray() {
    this.fixComma();
    b.append("[");
    this.openArrays++;
    return this;
  }

  /**
   * <p>startArrayProperty.</p>
   *
   * @param _property a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder startArrayProperty(String _property) {
    this.fixComma();
    b.append(JsonBuilder.escape(_property)).append(":[");
    this.openArrays++;
    return this;
  }

  /**
   * <p>endArray.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder endArray() {
    b.append("]");
    this.openArrays--;
    return this;
  }

  /**
   * <p>startObject.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder startObject() {
    this.fixComma();
    b.append("{");
    this.openObjects++;
    return this;
  }

  /**
   * <p>startObjectProperty.</p>
   *
   * @param _property a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder startObjectProperty(String _property) {
    this.fixComma();
    b.append(JsonBuilder.escape(_property)).append(":{");
    this.openObjects++;
    return this;
  }

  /**
   * <p>endObject.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder endObject() {
    b.append("}");
    this.openObjects--;
    return this;
  }

  private boolean isSelfContainedJson(String _json) {
    final String json = _json.trim();
    if (json.startsWith("{"))
      if (!json.endsWith("}")) return false;
      else if (json.startsWith("["))
        if (!json.endsWith("]")) return false;
        else return false;
    return true;
  }

  /**
   * Appends a String to an already opened array.
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendToArray(String _string) {
    this.fixComma();
    this.b.append(JsonBuilder.escape(_string));
    return this;
  }

  /**
   * Appends an Integer to an already opened array.
   *
   * @param _i a {@link java.lang.Integer} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendToArray(Integer _i) {
    this.fixComma();
    this.b.append(_i);
    return this;
  }

  /**
   * Appends a self-contained JSON array or object to an already opened array.
   *
   * @param _json a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendJsonToArray(String _json) {
    this.fixComma();
    this.b.append(_json);
    return this;
  }

  /**
   * <p>appendObjectProperty.</p>
   *
   * @param _property a {@link java.lang.String} object.
   * @param _value a {@link java.lang.Integer} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendObjectProperty(String _property, Integer _value) {
    this.fixComma();
    b.append(JsonBuilder.escape(_property)).append(":");
    if (_value == null) b.append("null");
    else b.append(_value);
    return this;
  }

  /**
   * <p>appendObjectProperty.</p>
   *
   * @param _property a {@link java.lang.String} object.
   * @param _value a {@link java.lang.Boolean} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendObjectProperty(String _property, Boolean _value) {
    this.fixComma();
    b.append(JsonBuilder.escape(_property)).append(":");
    if (_value == null) b.append("null");
    else b.append(_value.booleanValue());
    return this;
  }

  /**
   * Appends an object property with the given name and value, whereby the value is escaped.
   *
   * @param _property a {@link java.lang.String} object.
   * @param _value a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendObjectProperty(String _property, String _value) {
    return this.appendObjectProperty(_property, _value, true);
  }

  /**
   * Appends an object property with the given name and value. The value is escaped depending on the boolean parameter _escape.
   *
   * @param _property a {@link java.lang.String} object.
   * @param _value a {@link java.lang.String} object.
   * @param _escape a boolean.
   * @return a {@link org.eclipse.steady.shared.json.JsonBuilder} object.
   */
  public JsonBuilder appendObjectProperty(String _property, String _value, boolean _escape) {
    this.fixComma();
    b.append(JsonBuilder.escape(_property)).append(":");
    if (_value == null) b.append("null");
    else if (_escape) b.append(JsonBuilder.escape(_value));
    //		else if(!this.isSelfContainedJson(_value))
    //			throw new JsonFormatException("Value is expected to be either a self-contained JSON array
    // enclosed by [ and ], or object enclosed by { and }");
    else b.append(_value);
    return this;
  }

  /**
   * <p>getJson.</p>
   *
   * @param _pretty a boolean.
   * @param _indent a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String getJson(boolean _pretty, String _indent) {
    if (!_pretty) return this.toString();
    else {
      final String ugly = this.toString();
      char c = 0;
      int i;
      int len = ugly.length();
      int indent_count = 0;
      boolean in_quoted_string = false;
      final StringBuilder pretty = new StringBuilder();
      for (i = 0; i < len; i += 1) {
        c = ugly.charAt(i);
        switch (c) {
          case '"':
            in_quoted_string = !in_quoted_string;
            pretty.append(c);
            break;
          case '{':
            indent_count++;
            pretty.append("{\n").append(this.indent(indent_count, _indent));
            break;
          case '}':
            indent_count--;
            pretty.append("\n").append(this.indent(indent_count, _indent)).append("}");
            break;
          case '[':
            if (in_quoted_string) pretty.append(c);
            else {
              indent_count++;
              pretty.append("[\n").append(this.indent(indent_count, _indent));
            }
            break;
          case ']':
            if (in_quoted_string) pretty.append(c);
            else {
              indent_count--;
              pretty.append("\n").append(this.indent(indent_count, _indent)).append("]");
            }
            break;
          case ',':
            if (in_quoted_string) pretty.append(c);
            else pretty.append(",\n").append(this.indent(indent_count, _indent));
            break;
          default:
            pretty.append(c);
        }
      }
      return pretty.toString();
    }
  }

  private String indent(int _count, String _indent) {
    final StringBuilder b = new StringBuilder();
    for (int i = 0; i < _count; i++) b.append(_indent);
    return b.toString();
  }

  /**
   * <p>getJson.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getJson() {
    return this.getJson(false, "");
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return b.toString();
  }

  /**
   * <p>checkValidity.</p>
   *
   * @throws org.eclipse.steady.shared.json.JsonSyntaxException if any.
   */
  public void checkValidity() throws JsonSyntaxException {
    JsonBuilder.checkJsonValidity(this.getJson());
  }

  /**
   * <p>checkJsonValidity.</p>
   *
   * @param _json a {@link java.lang.String} object.
   * @throws org.eclipse.steady.shared.json.JsonSyntaxException if any.
   */
  public static void checkJsonValidity(String _json) throws JsonSyntaxException {
    //		final JsonParser p = new JsonParser();
    //		p.parse(_json);
  }

  /**
   * Escapes JSON special characters in the given string, and encloses all of it in double quotes.
   * The method body has been copied from "org.codehaus.jettison.json.JSONObject.quote(String)".
   *
   * @param _string to be escaped
   * @return escaped string
   */
  public static String escape(String _string) {
    if (_string == null || _string.length() == 0) {
      return "\"\"";
    }

    char c = 0;
    int i;
    int len = _string.length();
    final StringBuilder sb = new StringBuilder(len + 4);
    String t;

    sb.append('"');
    for (i = 0; i < len; i += 1) {
      c = _string.charAt(i);
      switch (c) {
        case '\\':
        case '"':
          sb.append('\\');
          sb.append(c);
          break;
        case '/':
          //                if (b == '<') {
          sb.append('\\');
          //                }
          sb.append(c);
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\r':
          sb.append("\\r");
          break;
        default:
          if (c < ' ') {
            t = "000" + Integer.toHexString(c);
            sb.append("\\u" + t.substring(t.length() - 4));
          } else {
            sb.append(c);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }
}
