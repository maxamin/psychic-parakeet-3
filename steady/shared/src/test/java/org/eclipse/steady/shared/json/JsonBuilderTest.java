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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JsonBuilderTest {

  /**
   *
   */
  @Test
  public void testJsonBuilder() {
    JsonBuilder b = new JsonBuilder();
    String pretty_json = null, json = null;
    b.startObject().startArrayProperty("array_property");
    b.startObject().appendObjectProperty("string_property", "string value").endObject();
    b.startObject().appendObjectProperty("int_property1", (Integer) null).endObject();
    b.startObject().appendObjectProperty("int_property2", new Integer(3)).endObject();
    b.endArray().endObject();

    pretty_json = b.getJson(true, "    ");
    assertEquals(
        "{\n"
            + "    \"array_property\":[\n"
            + "        {\n"
            + "            \"string_property\":\"string value\"\n"
            + "        },\n"
            + "        {\n"
            + "            \"int_property1\":null\n"
            + "        },\n"
            + "        {\n"
            + "            \"int_property2\":3\n"
            + "        }\n"
            + "    ]\n"
            + "}",
        pretty_json);

    json = b.getJson();
    assertEquals(
        "{\"array_property\":[{\"string_property\":\"string"
            + " value\"},{\"int_property1\":null},{\"int_property2\":3}]}",
        json);
  }

  /**
   *
   */
  @Test // l( expected = JsonSyntaxException.class)
  public void testSyntaxValidation() {
    final String invalid_json = "{ \"key1\" : \"value1\", }";
    // JsonBuilder.checkJsonValidity(invalid_json);
  }
}
