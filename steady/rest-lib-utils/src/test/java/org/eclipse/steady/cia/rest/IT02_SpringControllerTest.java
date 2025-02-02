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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainController.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@WebAppConfiguration
@ActiveProfiles("test")
public class IT02_SpringControllerTest {

  private MockMvc mockMvc;
  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  private MediaType contentType =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  private static String getArtifactUri(String g, String a, String v) {
    String s = "/artifacts/" + g + "/" + a;
    s = ((v != null) ? s += "/" + v : s);
    return s;
  }

  private static String isWellknownSha1(String sha1) {
    return "/artifacts/" + sha1;
  }

  private static String getConstructUri(String g, String a, String v, String t, String qname) {
    String s = "/constructs/" + g + "/" + a + "/" + v + "/" + t + "/" + qname;
    return s;
  }

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter =
        Arrays.asList(converters).stream()
            .filter(
                new Predicate<HttpMessageConverter<?>>() {
                  @Override
                  public boolean test(HttpMessageConverter<?> hmc) {
                    return hmc instanceof MappingJackson2HttpMessageConverter;
                  }
                })
            .findAny()
            .get();

    Assert.assertNotNull(
        "the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
  }

  @After
  public void reset() throws Exception {}

  @Test
  public void getAllVersionsTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(getArtifactUri("commons-fileupload", "commons-fileupload", null));
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
    final MockHttpServletRequestBuilder get_builder1 =
        get(getArtifactUri("Django", "Django", null));
    mockMvc
        .perform(get_builder1)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
  }

  @Test
  public void getAllVersionsNotFoundTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(getArtifactUri("commons-fileupload", "commons-", null));
    mockMvc.perform(get_builder).andExpect(status().isNotFound());
  }

  @Test
  public void getLatestVersionsTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(getArtifactUri("commons-fileupload", "commons-fileupload", null).concat("/latest"));
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
  }

  @Test
  public void getLatestVersionsNotFoundTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(getArtifactUri("commons-fileupload", "commons-", null).concat("/latest"));
    mockMvc.perform(get_builder).andExpect(status().isNotFound());
  }

  @Test
  public void getLatestVersionsPackagingFilterTest() throws Exception {
    final MockHttpServletRequestBuilder get_builder2 =
        get(
            getArtifactUri("com.fasterxml.jackson.core", "jackson-databind", null)
                .concat("/latest?packagingFilter=jar"));
    mockMvc
        .perform(get_builder2)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
    // .andExpect(jsonPath("$.libId.version", is("2.9.5")));
  }

  @Test
  public void getLatestVersionsPackagingFilterPythonTest() throws Exception {
    final MockHttpServletRequestBuilder get_builder2 =
        get(getArtifactUri("Django", "Django", null).concat("/latest?packagingFilter=sdist"));
    mockMvc
        .perform(get_builder2)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
    // .andExpect(jsonPath("$.libId.version", is("2.9.5")));
  }

  @Test
  public void getByDigestTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(isWellknownSha1("A3665CF8E3426686EE751790F3D1E1EC5705E9DC"));
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.libId.artifact", is("ognl")))
        .andExpect(jsonPath("$.libId.version", is("3.0.6")));
  }

  @Test
  public void getByDigestNotFoundTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(isWellknownSha1("7F7798C34114BF620EFA99DFF6770C458234FDBC"));
    mockMvc.perform(get_builder).andExpect(status().isNotFound());
  }

  @Test
  public void getGreaterVersionsTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(
            getArtifactUri("commons-fileupload", "commons-fileupload", null)
                .concat("/greaterThan/1.2.2"));
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));
    // .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(7)));

  }

  @Test
  public void doesVersionSourceExistTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(
            getArtifactUri("commons-fileupload", "commons-fileupload", "1.3.3")
                .concat("?classifier=sources"));
    mockMvc.perform(get_builder).andExpect(status().isOk());

    final MockHttpServletRequestBuilder get_builder1 =
        get(
            getArtifactUri("commons-fileupload", "commons-fileupload", "1.1.1")
                .concat("?classifier=sources"));
    mockMvc.perform(get_builder1).andExpect(status().isNotFound());
  }

  @Test
  public void downloadVersionTest() throws Exception {

    // Artifact created to check if the artifact is already existing locally in which case it is
    // deleted to make sure the test can run
    // it is ensured that the test does not alter the state of the system (if the file (not) existed
    // before the execution it will (not) exist after)
    Artifact toDownload = new Artifact("commons-fileupload", "commons-fileupload", "1.3.3");
    toDownload.setClassifier("sources");
    toDownload.setPackaging("jar");
    toDownload.setProgrammingLanguage(ProgrammingLanguage.JAVA);

    boolean existing = false;
    // delete artifact if already existing
    if (toDownload.getAbsM2Path().toFile().exists()) {
      existing = true;
      toDownload.getAbsM2Path().toFile().delete();
    }

    final MockHttpServletRequestBuilder get_builder =
        get(
            getArtifactUri("commons-fileupload", "commons-fileupload", "1.3.3")
                .concat("/jar?classifier=sources"));
    mockMvc.perform(get_builder).andExpect(status().isOk());

    if (!existing && toDownload.getAbsM2Path().toFile().exists())
      toDownload.getAbsM2Path().toFile().delete();

    final MockHttpServletRequestBuilder get_builder1 =
        get(
            getArtifactUri("commons-fileupload", "commons-fileupload", "1.1.1")
                .concat("/jar?classifier=sources"));
    mockMvc.perform(get_builder1).andExpect(status().isNotFound());
  }

  @Test
  public void getConstructforGavTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        get(
            getConstructUri(
                "org.apache.cxf",
                "cxf-rt-rs-extension-providers",
                "2.6.2",
                "METH",
                "org.apache.cxf.jaxrs.provider.atom.AbstractAtomProvider.readFrom(Class,Type,Annotation[],MediaType,MultivaluedMap,InputStream)"));
    mockMvc.perform(get_builder).andExpect(status().isOk());
  }

  @Test
  public void getConstructSourceforGavTest() throws Exception {
    final MockHttpServletRequestBuilder get_builder =
        get(
            getConstructUri(
                "org.apache.xmlgraphics",
                "batik-dom",
                "1.9.1",
                "METH",
                "org.apache.batik.dom.util.SAXDocumentFactory.createDocument(InputSource)"));
    mockMvc.perform(get_builder).andExpect(status().isOk());
  }

  @Test
  public void intersectNotExistingArtifactTest() throws Exception {

    final MockHttpServletRequestBuilder get_builder =
        post(getArtifactUri("corg.eclipse.jetty.aggregate", "jetty-all", "9.4.13.v20181111")
                .concat("/jar/constructIds/intersect?lang=JAVA"))
            .content("[]")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(get_builder).andExpect(status().isNotFound());
  }

  @Test
  public void getConstructSignatureTest() throws Exception {
    final MockHttpServletRequestBuilder get_builder =
        get(
            getConstructUri(
                "org.apache.tomcat.embed",
                "tomcat-embed-core",
                "7.0.0",
                "METH",
                "org.apache.catalina.filters.CsrfPreventionFilter.doFilter(ServletRequest,ServletResponse,FilterChain)"
                    + "/sign?sources=true&lang=JAVA"));
    mockMvc.perform(get_builder).andExpect(status().isOk());
  }
}
