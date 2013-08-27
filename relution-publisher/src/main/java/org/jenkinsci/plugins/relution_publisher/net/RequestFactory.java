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

import org.jenkinsci.plugins.relution_publisher.config.global.Store;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApplicationResponse;


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

    private static <T extends ApiResponse> Request<T> createBaseRequest(final int method, final Class<T> responseClass, final Store store,
            final String... parts) {

        final String url = RequestFactory.getUrl(store, parts);
        final Request<T> request = new Request<T>(method, url, responseClass);

        request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", "Basic " + store.getAuthorizationToken());

        return request;
    }

    public static Request<ApplicationResponse> createAppStoreItemsRequest(final Store store) {

        final Request<ApplicationResponse> request = RequestFactory.createBaseRequest(
                Request.Method.GET,
                ApplicationResponse.class,
                store,
                URL_APP_STORE_ITEMS);

        request.queryFields().add("locale", "de");

        return request;
    }
}
