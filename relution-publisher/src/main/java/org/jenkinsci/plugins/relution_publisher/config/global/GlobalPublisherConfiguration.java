/*
 * Copyright (c) 2013 M-Way Solutions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.plugins.relution_publisher.config.global;

import hudson.Extension;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

import jenkins.model.GlobalConfiguration;


/**
 * Saves and restores the global configuration used by the plugin.
 */
@Extension
public class GlobalPublisherConfiguration extends GlobalConfiguration {

    public final static String KEY_STORES     = "stores";
    public final static String KEY_PROXY_URL  = "proxyHost";
    public final static String KEY_PROXY_PORT = "proxyPort";

    private final List<Store>  stores         = new ArrayList<Store>();

    private String             proxyHost;
    private int                proxyPort;

    /**
     * Initializes a new instance of the {@link GlobalPublisherConfiguration} class.
     */
    @DataBoundConstructor
    public GlobalPublisherConfiguration() {
        this.load();
    }

    private void addStores(final JSONArray storesJsonArray) {

        for (int n = 0; n < storesJsonArray.size(); n++) {
            final JSONObject store = storesJsonArray.getJSONObject(n);
            this.addStore(store);
        }
    }

    private void addStore(final JSONObject storeJsonObject) {

        final Store store = new Store(storeJsonObject);
        this.stores.add(store);
    }

    /**
     * This human readable name is used in the configuration screen.
     */
    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
        System.out.println(json.toString());

        this.stores.clear();
        final Object jsonEntity = json.get(KEY_STORES);

        if (jsonEntity instanceof JSONArray) {
            final JSONArray stores = (JSONArray) jsonEntity;
            this.addStores(stores);

        } else if (jsonEntity instanceof JSONObject) {
            final JSONObject store = (JSONObject) jsonEntity;
            this.addStore(store);
        }

        this.proxyHost = json.getString(KEY_PROXY_URL);
        this.proxyPort = json.optInt(KEY_PROXY_PORT, 0);

        this.save();
        return false;
    }

    /**
     * Gets the list of {@link Store}s configured for this publisher.
     */
    public List<Store> getStores() {
        return this.stores;
    }

    /**
     * Gets the store with the specified identifier.
     * @param storeId A store's {@link Store#getIdentifier() identifier}.
     * @return The {@link Store} with the specified id, or <code>null</code> if no such store
     * exists.
     */
    public Store getStore(final String storeId) {

        if (this.stores == null) {
            return null;
        }

        for (final Store store : this.stores) {
            if (store.getIdentifier().equals(storeId)) {
                return store;
            }
        }
        return null;
    }

    /**
     * Gets the host name of the proxy server to use.
     */
    public String getProxyHost() {
        return this.proxyHost;
    }

    /**
     * Sets the host name of the proxy server to use.
     * @param proxyHost A host name.
     */
    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Gets the port number of the proxy server to use.
     */
    public int getProxyPort() {
        return this.proxyPort;
    }

    /**
     * Sets the port number of the proxy server to use.
     * @param proxyPort A port number.
     */
    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
