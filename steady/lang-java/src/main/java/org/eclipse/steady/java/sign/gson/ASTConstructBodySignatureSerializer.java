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
package org.eclipse.steady.java.sign.gson;

import java.io.IOException;

import org.eclipse.steady.java.sign.ASTConstructBodySignature;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * <p>ASTConstructBodySignatureSerializer class.</p>
 */
public class ASTConstructBodySignatureSerializer extends StdSerializer<ASTConstructBodySignature> {

  /**
   * <p>Constructor for ASTConstructBodySignatureSerializer.</p>
   */
  public ASTConstructBodySignatureSerializer() {
    this(null);
  }

  /**
   * <p>Constructor for ASTConstructBodySignatureSerializer.</p>
   *
   * @param t a {@link java.lang.Class} object.
   */
  public ASTConstructBodySignatureSerializer(Class<ASTConstructBodySignature> t) {
    super(t);
  }

  /**
   * @param n - The Root Node
   * @return - JSON representation of the AST
   *  NB : An ast as an array of Nodes
   *   A Node {"name" : "Value of Node", "parent" : "value of parent Node", "children" : [{"name" : "value of node", "parent" : "value of parent Node"},......,{}]}
   */
  private void writeComplexNode(JsonGenerator jgen, Node n) throws IOException {
    if (n.isLeaf()) {
      jgen.writeStartObject();
      this.writeNodeProperties(jgen, n);
      jgen.writeEndObject();
    } else {
      for (int i = 0; i < n.getChildCount(); i++) {
        final Node node = (Node) n.getChildAt(i);
        jgen.writeStartObject();
        this.writeNodeProperties(jgen, node);
        if (!node.isLeaf()) {
          jgen.writeArrayFieldStart("C");
          this.writeComplexNode(jgen, node);
          jgen.writeEndArray();
        }
        jgen.writeEndObject();
      }
    }
  }

  /**
   * Writes various properties of the given {@link Node}.
   * @param n the node whose properties are written
   */
  private void writeNodeProperties(JsonGenerator jgen, Node n) throws IOException {
    jgen.writeStringField("Value", n.getValue().toString());
    // The unique name has the same information as Node.Value
    // buffer.append("\"UniqueName\" : "
    // ).append(JsonBuilder.escape(n.getEntity().getUniqueName().toString())).append(","); ;

    jgen.writeObjectFieldStart("SourceCodeEntity");
    jgen.writeStringField("Modifiers", Integer.toString(n.getEntity().getModifiers()));

    jgen.writeObjectFieldStart("SourceRange");
    jgen.writeStringField("Start", Integer.toString(n.getEntity().getSourceRange().getStart()));
    jgen.writeStringField("End", Integer.toString(n.getEntity().getSourceRange().getEnd()));
    jgen.writeEndObject(); // SourceRange

    jgen.writeEndObject(); // SourceCodeEntity
    jgen.writeStringField("EntityType", n.getEntity().getType().toString());
  }

  /** {@inheritDoc} */
  @Override
  public void serialize(
      ASTConstructBodySignature value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    jgen.writeArrayFieldStart("ast");
    final Node n = value.getRoot();
    if (n.isRoot()) {
      jgen.writeStartObject();
      this.writeNodeProperties(jgen, n);
      if (!n.isLeaf()) {
        jgen.writeArrayFieldStart("C");
        this.writeComplexNode(jgen, n);
        jgen.writeEndArray();
      }
      jgen.writeEndObject();
    }
    jgen.writeEndArray();
    jgen.writeEndObject();
  }
}
