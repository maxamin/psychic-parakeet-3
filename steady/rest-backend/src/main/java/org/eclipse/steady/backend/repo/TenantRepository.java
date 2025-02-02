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

import java.util.List;

import org.eclipse.steady.backend.model.Tenant;
import org.eclipse.steady.backend.util.ResultSetFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>TenantRepository interface.</p>
 */
@Repository
public interface TenantRepository extends CrudRepository<Tenant, Long>, TenantRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Tenant> FILTER = new ResultSetFilter<Tenant>();

  /**
   * <p>findBySecondaryKey.</p>
   *
   * @param token a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT s FROM Tenant AS s WHERE s.tenantToken = :token")
  List<Tenant> findBySecondaryKey(@Param("token") String token);

  /**
   * <p>findDefault.</p>
   *
   * @return a {@link org.eclipse.steady.backend.model.Tenant} object.
   */
  @Query("SELECT s FROM Tenant AS s WHERE s.isDefault=true")
  Tenant findDefault();
}
