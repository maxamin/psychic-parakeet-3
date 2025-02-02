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
package org.eclipse.steady.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.function.Predicate;

import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.backend.repo.LibraryRepository;
import org.eclipse.steady.shared.categories.RequiresNetwork;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.util.FileUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainController.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class LibraryControllerTest {

  private MediaType contentType =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  private MockMvc mockMvc;
  private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

  @Autowired private LibraryRepository libRepository;

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
    this.libRepository.deleteAll();
  }

  /**
   * 2x rest-post and rest-get (using Jinja2-2.9.6)
   * @throws Exception
   */
  @Test
  @Category(RequiresNetwork.class)
  public void testPostJinja2() throws Exception {
    // Rest-post 1.2.2
    Library lib =
        (Library)
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get("./src/test/resources/real_examples/lib_Jinja2-2.9.6.json")),
                Library.class);
    MockHttpServletRequestBuilder post_builder =
        post("/libs/")
            .content(JacksonUtil.asJsonString(lib).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.wellknownDigest", is(true)))
        .andExpect(jsonPath("$.digest", is("61A215BCDB0F7939C70582BC00B293F1")));

    // Repo must contain 1
    assertEquals(1, this.libRepository.count());
  }

  /**
   * 2x rest-post and rest-get for a library with bundled libIds (org.springframework.cloud:spring-cloud-cloudfoundry-connector)
   * @throws Exception
   */
  @Test
  @Category(RequiresNetwork.class)
  public void testPostLibWithBundledLibIds() throws Exception {
    // Rest-post 1.2.2
    Library lib =
        (Library)
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get("./src/test/resources/real_examples/lib_bundledLibIds.json")),
                Library.class);
    MockHttpServletRequestBuilder post_builder =
        post("/libs/")
            .content(JacksonUtil.asJsonString(lib).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.wellknownDigest", is(true)))
        .andExpect(jsonPath("$.bundledLibraryIds", hasSize(3)));

    // Repo must contain 1
    assertEquals(1, this.libRepository.count());
  }

  /**
   * 2x rest-post and rest-get (using different versions of Apache Commons-Fileupload)
   * @throws Exception
   */
  @Test
  @Category(RequiresNetwork.class)
  public void testPostCommonsFileUpload() throws Exception {
    // Rest-post 1.2.2
    Library lib =
        (Library)
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get(
                        "./src/test/resources/real_examples/lib_commons-fileupload-1.2.2.json")),
                Library.class);
    MockHttpServletRequestBuilder post_builder =
        post("/libs/")
            .content(JacksonUtil.asJsonString(lib).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.wellknownDigest", is(true)))
        .andExpect(jsonPath("$.bundledLibraryIds", hasSize(1)))
        .andExpect(jsonPath("$.digest", is("1E48256A2341047E7D729217ADEEC8217F6E3A1A")));

    // Repo must contain 1
    assertEquals(1, this.libRepository.count());

    lib =
        (Library)
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get(
                        "./src/test/resources/real_examples/lib_commons-fileupload-1.3.1.json")),
                Library.class);
    post_builder =
        post("/libs/")
            .content(JacksonUtil.asJsonString(lib).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.digest", is("C621B54583719AC0310404463D6D99DB27E1052C")));

    assertEquals(2, this.libRepository.count());

    // check that modifiedAt does not get updated on GET
    Library managed_lib =
        LibraryRepository.FILTER.findOne(
            libRepository.findByDigest("1E48256A2341047E7D729217ADEEC8217F6E3A1A"));
    Calendar modifiedAt = managed_lib.getModifiedAt();
    managed_lib =
        LibraryRepository.FILTER.findOne(
            libRepository.findByDigest("1E48256A2341047E7D729217ADEEC8217F6E3A1A"));
    assertTrue(modifiedAt.getTimeInMillis() == managed_lib.getModifiedAt().getTimeInMillis());
  }

  /**
   * post lib with well known digest, not well known libid
   * @throws Exception
   */
  @Test
  @Category(RequiresNetwork.class)
  public void testPostNotWellKnownLibId() throws Exception {

    Library lib = new Library("1E48256A2341047E7D729217ADEEC8217F6E3A1A");
    lib.setDigestAlgorithm(DigestAlgorithm.SHA1);
    lib.setLibraryId(new LibraryId("bar", "bar", "0"));
    MockHttpServletRequestBuilder post_builder =
        post("/libs/")
            .content(JacksonUtil.asJsonString(lib).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.wellknownDigest", is(true)))
        .andExpect(jsonPath("$.libraryId.artifact", is("commons-fileupload")))
        .andExpect(jsonPath("$.digest", is("1E48256A2341047E7D729217ADEEC8217F6E3A1A")));
  }
}
