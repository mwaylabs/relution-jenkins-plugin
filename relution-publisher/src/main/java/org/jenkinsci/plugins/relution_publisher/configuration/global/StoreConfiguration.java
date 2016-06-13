/*
 * Copyright (c) 2013-2014 M-Way Solutions GmbH
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

package org.jenkinsci.plugins.relution_publisher.configuration.global;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;


/**
 * Saves and restores the global configuration used by the plugin.
 */
@Extension
public class StoreConfiguration extends GlobalConfiguration {

    public final static String KEY_STORES        = "stores";
    public final static String KEY_DEBUG_ENABLED = "debugEnabled";

    private final List<Store>  stores            = new ArrayList<Store>();

    private Boolean            isDebugEnabled;

    /**
     * Initializes a new instance of the {@link StoreConfiguration} class.
     */
    @DataBoundConstructor
    public StoreConfiguration() {
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

        this.isDebugEnabled = json.getBoolean(KEY_DEBUG_ENABLED);

        this.save();
        return false;
    }

    /**
     * @return The list of {@link Store}s configured for this publisher.
     */
    public List<Store> getStores() {
        return this.stores;
    }

    /**
     * Gets the store with the specified identifier.
     * @param storeId A store's {@link Store#getId() identifier}.
     * @return The {@link Store} with the specified id, or {@code null} if no such store
     * exists.
     */
    public Store getStore(final String storeId) {
        if (this.stores == null) {
            return null;
        }

        for (final Store store : this.stores) {
            if (StringUtils.equals(storeId, store.getId())) {
                return store;
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether debug output should be enabled.
     * @return {@code true} if debug output should be enabled; otherwise, {@code false}.
     */
    public Boolean isDebugEnabled() {
        return this.isDebugEnabled;
    }

    /**
     * Sets a value indicating whether debug output should be enabled.
     * @param enabled {@code true} to enable debug output; {@code false} to disable debug output.
     */
    public void setDebugEnabled(final boolean enabled) {
        this.isDebugEnabled = enabled;
    }
}
