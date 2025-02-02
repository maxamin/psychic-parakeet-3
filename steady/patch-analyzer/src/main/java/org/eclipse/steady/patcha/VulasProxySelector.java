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
package org.eclipse.steady.patcha;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.shared.util.StringList.CaseSensitivity;
import org.eclipse.steady.shared.util.StringList.ComparisonMode;

/**
 * <p>VulasProxySelector class.</p>
 */
public class VulasProxySelector extends ProxySelector {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static ProxySelector instance = new VulasProxySelector();

  /**
   * <p>registerAsDefault.</p>
   */
  public static void registerAsDefault() {
    ProxySelector.setDefault(VulasProxySelector.instance);
  }

  private Proxy httpProxy = null;

  private Configuration cfg = null;

  // private Set<String> noProxyHosts = new HashSet<String>();

  private StringList noProxyHosts = new StringList();

  ProxySelector def = null;

  /**
   * <p>Constructor for VulasProxySelector.</p>
   */
  public VulasProxySelector() {

    // Remember current default (fallback solution)
    this.def = ProxySelector.getDefault();

    // Read configuration
    this.cfg = VulasConfiguration.getGlobal().getConfiguration();

    // No proxy for the following hosts
    noProxyHosts.addAll(this.cfg.getString("http.nonProxyHosts", ""), "\\|", true);
    // String[] no_proxy_hosts = this.cfg.getString("http.nonProxyHosts", "").split("\\|");
    // for(int i=0; i<no_proxy_hosts.length; i++) this.noProxyHosts.add(no_proxy_hosts[i]);

    // Create HTTP proxy
    if (this.cfg.getString("http.proxyHost") != null
        && !this.cfg.getString("http.proxyHost").equals("")
        && this.cfg.getString("http.proxyPort") != null
        && !this.cfg.getString("http.proxyPort").equals("")) {
      this.httpProxy =
          new Proxy(
              Proxy.Type.HTTP,
              new InetSocketAddress(
                  this.cfg.getString("http.proxyHost"), this.cfg.getInt("http.proxyPort")));
      VulasProxySelector.log.info(
          "Proxy selector configuration: ["
              + this.cfg.getString("http.proxyHost")
              + ":"
              + this.cfg.getInt("http.proxyPort")
              + ", non proxy hosts: "
              + this.noProxyHosts.toString()
              + "]");
    } else {
      VulasProxySelector.log.info("Proxy selector configuration: None");
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Proxy> select(URI uri) {
    List<Proxy> l = null;
    if (this.noProxyHosts.contains(
        uri.getHost(), ComparisonMode.PATTERN, CaseSensitivity.CASE_INSENSITIVE)) {
      VulasProxySelector.log.info("No proxy for URL [" + uri + "]");
      l = Arrays.asList(Proxy.NO_PROXY);
    } else {
      if (this.httpProxy != null
          && (uri.getScheme().equalsIgnoreCase("http")
              || uri.getScheme().equalsIgnoreCase("https"))) {
        VulasProxySelector.log.info("Using proxy [" + this.httpProxy + "] for URL [" + uri + "]");
        l = Arrays.asList(this.httpProxy);
      } else {
        l = this.def.select(uri);
      }
    }
    return l;
  }

  /** {@inheritDoc} */
  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    if (uri == null || sa == null || ioe == null) {
      throw new IllegalArgumentException("Arguments can not be null.");
    }
  }
}
