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

package org.jenkinsci.plugins.relution_publisher.net;

import org.jenkinsci.plugins.relution_publisher.config.Store;


public final class RequestFactory {

    private final static String URL_APP_STORE_ITEMS = "apps";

    private RequestFactory() {
    }

    private static String getUrl(final Store store, final String... parts) {

        final StringBuilder sb = new StringBuilder();
        sb.append(store.getUrl());

        for (final String part : parts) {
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append("/");
            }
            sb.append(part);
        }

        return sb.toString();
    }

    private static Request createBaseRequest(final int method, final Store store, final String... parts) {

        final String url = RequestFactory.getUrl(store, parts);
        final Request request = new Request(method, url);

        request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", "Basic " + store.getAuthorizationToken());

        return request;
    }

    public static Request createAppStoreItemsRequest(final Store store) {

        final Request request = RequestFactory.createBaseRequest(
                Request.Method.GET,
                store,
                URL_APP_STORE_ITEMS);

        request.queryFields().add("locale", "de");

        return request;
    }
}
