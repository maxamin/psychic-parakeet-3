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
package org.eclipse.steady.backend.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.model.Dependency;
import org.eclipse.steady.backend.model.Trace;
import org.eclipse.steady.backend.util.ReferenceUpdater;
import org.eclipse.steady.shared.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>TracesRepositoryImpl class.</p>
 */
public class TracesRepositoryImpl implements TracesRepositoryCustom {

  private static Logger log = LoggerFactory.getLogger(TracesRepositoryImpl.class);

  @Autowired ApplicationRepository appRepository;

  @Autowired LibraryRepository libRepository;

  @Autowired ConstructIdRepository cidRepository;

  @Autowired TracesRepository traceRepository;

  @Autowired ReferenceUpdater refUpdater;

  @Autowired DependencyRepository depRepository;

  /**
   * <p>customSave.</p>
   *
   * @return the saved bug
   * @param _app a {@link org.eclipse.steady.backend.model.Application} object.
   * @param _traces an array of {@link org.eclipse.steady.backend.model.Trace} objects.
   * @throws javax.persistence.PersistenceException if any.
   */
  public List<Trace> customSave(Application _app, Trace[] _traces) throws PersistenceException {
    final StopWatch sw =
        new StopWatch("Save [" + _traces.length + "] traces for app " + _app).start();

    // To be returned
    List<Trace> traces = new ArrayList<Trace>();

    // Helpers needed for updating the properties of the provided traces
    Trace managed_trace = null;
    ConstructId managed_cid = null;
    Dependency managed_dep = null;

    // Avoid lib queries for every trace
    Map<String, Dependency> deps_cache = new HashMap<String, Dependency>();

    for (Trace provided_trace : _traces) {

      // Does it already exist?
      try {
        // Set managed objects
        provided_trace.setApp(_app);
        if (provided_trace.getLib() != null) // Lib can be null if trace belongs to app construct
        provided_trace.setLib(
              LibraryRepository.FILTER.findOne(
                  this.libRepository.findByDigest(provided_trace.getLib().getDigest())));
        provided_trace.setConstructId(
            ConstructIdRepository.FILTER.findOne(
                this.cidRepository.findConstructId(
                    provided_trace.getConstructId().getLang(),
                    provided_trace.getConstructId().getType(),
                    provided_trace.getConstructId().getQname())));

        // Find existing trace (if any)
        if (provided_trace.getLib() != null)
          managed_trace =
              TracesRepository.FILTER.findOne(
                  this.traceRepository.findTracesOfLibraryConstruct(
                      provided_trace.getApp(),
                      provided_trace.getLib(),
                      provided_trace.getConstructId()));
        else
          // Find existing application trace (if any)
          managed_trace =
              TracesRepository.FILTER.findOne(
                  this.traceRepository.findTracesOfAppConstruct(
                      provided_trace.getApp(), provided_trace.getConstructId()));

        // Found existing trace, now merge count and trace times
        provided_trace.setId(managed_trace.getId());
        provided_trace.setCount(managed_trace.getCount() + provided_trace.getCount());
      } catch (EntityNotFoundException e1) {
      }

      // Care for traces who's constructs have not been uploaded yet
      try {
        managed_cid =
            ConstructIdRepository.FILTER.findOne(
                this.cidRepository.findConstructId(
                    provided_trace.getConstructId().getLang(),
                    provided_trace.getConstructId().getType(),
                    provided_trace.getConstructId().getQname()));
      } catch (EntityNotFoundException e1) {
        managed_cid = this.cidRepository.save(provided_trace.getConstructId());
      }
      provided_trace.setConstructId(managed_cid);

      // Check whether a corresponding dependency has been created already and set traced property
      if (provided_trace.getLib() != null) {
        try {
          // Get the dependency
          if (deps_cache.containsKey(provided_trace.getLib().getDigest())) {
            managed_dep = deps_cache.get(provided_trace.getLib().getDigest());
          } else {
            managed_dep =
                DependencyRepository.FILTER.findOne(
                    this.depRepository.findByAppAndLib(_app, provided_trace.getLib().getDigest()));
            deps_cache.put(provided_trace.getLib().getDigest(), managed_dep);
          }

          // Update traced property
          if (managed_dep.getTraced() == null || !managed_dep.getTraced()) {
            managed_dep.setTraced(true);
            this.depRepository.save(managed_dep);
          }
        } catch (EntityNotFoundException e1) {
          managed_dep =
              new Dependency(
                  _app, provided_trace.getLib(), null, null, provided_trace.getFilename());
          managed_dep.setTraced(true);
          managed_dep = this.depRepository.save(managed_dep);
          deps_cache.put(managed_dep.getLib().getDigest(), managed_dep);
        }
      }

      // TODO: Check traces whose libraries are not yet existing

      // Save
      try {
        managed_trace = this.traceRepository.save(provided_trace);
        traces.add(managed_trace);
      } catch (Exception e) {
        throw new PersistenceException(
            "Error while saving trace " + provided_trace + ": " + e.getMessage());
      }
    }
    // After all traces have been saved,
    // update the lastScan timestamp of the application (we already have a managed application here)
    appRepository.refreshLastScanbyApp(_app);

    sw.stop();
    return traces;
  }
}
