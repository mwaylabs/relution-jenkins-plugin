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

import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.entities.Application;
import org.jenkinsci.plugins.relution_publisher.entities.Asset;
import org.jenkinsci.plugins.relution_publisher.entities.Version;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApplicationResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.AssetResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.StringResponse;

import java.io.File;
import java.nio.charset.Charset;


/**
 * Provides static methods that can be used to create {@link Request}s that can be used to
 * communicate with the Relution Enterprise Appstore.
 */
public final class RequestFactory {

    private final static Charset CHARSET            = Charset.forName("UTF-8");

    /**
     * The URL used to request or persist {@link Application} objects. 
     */
    private final static String  URL_APPS           = "apps";

    /**
     * The URL used to request or persist {@link Version} objects.
     */
    private final static String  URL_VERSIONS       = "versions";

    /**
     * The URL used to request or persist {@link Asset} objects.
     */
    private final static String  URL_FILES          = "files";

    /**
     * The URL used to request the unpersisted {@link Application} object associated with a
     * previously uploaded {@link Asset}. 
     */
    private final static String  URL_APPS_FROM_FILE = "apps/fromFile";

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

    private static <T> Request<T> createBaseRequest(final int method, final Class<? extends ApiResponse<T>> responseClass, final Store store,
            final String... parts) {

        final String url = RequestFactory.getUrl(store, parts);
        final Request<T> request = new Request<T>(method, url, responseClass);

        request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", "Basic " + store.getAuthorizationToken());

        if (!StringUtils.isBlank(store.getProxyHost()) && store.getProxyPort() != 0) {
            request.setProxy(store.getProxyHost(), store.getProxyPort());
        }

        return request;
    }

    /**
     * Creates a {@link Request} that can be used to retrieve all {@link Application} objects
     * stored in the server's database. 
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to query all applications on the server.
     */
    public static Request<Application> createAppStoreItemsRequest(final Store store) {

        final Request<Application> request = RequestFactory.createBaseRequest(
                Request.Method.GET,
                ApplicationResponse.class,
                store,
                URL_APPS);

        request.queryFields().add("locale", "de");

        return request;
    }

    /**
     * Creates a {@link Request} that can be used to upload a {@link File} to the server.
     * @param store The {@link Store} this request should be executed against.
     * @param file The {@link File} to upload.
     * @return A request that can be used to upload a file to the server.
     */
    public static Request<Asset> createUploadRequest(final Store store, final File file) {

        final Request<Asset> request = RequestFactory.createBaseRequest(
                Request.Method.POST,
                AssetResponse.class,
                store,
                URL_FILES);

        final MultipartEntity entity = new MultipartEntity();
        entity.addPart("file", new FileBody(file));
        request.setEntity(entity);

        return request;
    }

    /**
     * Creates a {@link Request} that can be used to retrieve the {@link Application} associated
     * with the specified {@link Asset}.
     * @param store The {@link Store} this request should be executed against.
     * @param asset The {@link Asset} for which to retrieve the {@link Application}
     * @return A request that can be used to retrieved the application associated with an asset.
     */
    public static Request<Application> createAppFromFileRequest(final Store store, final Asset asset) {

        final Request<Application> request = RequestFactory.createBaseRequest(
                Request.Method.POST,
                ApplicationResponse.class,
                store,
                URL_APPS_FROM_FILE,
                asset.getUuid());

        return request;
    }

    /**
     * Creates a {@link Request} that can be used to persist the specified {@link Application}.
     * <p/>
     * This call must not be used to persist an application that has already been persisted. Doing
     * so results in undefined behavior. An application is unpersisted if its
     * {@link Application#getUuid() identifier} is <code>null</code>.
     * @param store The {@link Store} this request should be executed against.
     * @param app The {@link Application} to persist.
     * @return A request that can be used to persist the specified application.
     */
    public static Request<Application> createPersistApplicationRequest(final Store store, final Application app) {

        final Request<Application> request = RequestFactory.createBaseRequest(
                Request.Method.POST,
                ApplicationResponse.class,
                store,
                URL_APPS);

        request.addHeader("Content-Type", "application/json");

        final StringEntity entity = new StringEntity(app.toJson(), CHARSET);
        request.setEntity(entity);

        return request;
    }

    /**
     * Creates a {@link Request} that can be used to persist the specified {@link Version}.
     * <p/>
     * This call must not be used to persist a version that has already been persisted. Doing so
     * results in undefined behavior. A version is unpersisted if its
     * {@link Version#getUuid() identifier} is <code>null</code>. The {@link Application}
     * associated with the version must already be persisted.
     * @param store The {@link Store} this request should be executed against.
     * @param version The {@link Version} to persist.
     * @return A request that can be used to persist the specified version.
     */
    public static Request<Application> createPersistVersionRequest(final Store store, final Version version) {

        final Request<Application> request = RequestFactory.createBaseRequest(
                Request.Method.POST,
                ApplicationResponse.class,
                store,
                URL_APPS,
                version.getAppUuid(),
                URL_VERSIONS);

        request.addHeader("Content-Type", "application/json");

        final StringEntity entity = new StringEntity(version.toJson(), CHARSET);
        request.setEntity(entity);

        return request;
    }

    public static Request<String> createDeleteVersionRequest(final Store store, final Version version) {

        final Request<String> request = RequestFactory.createBaseRequest(
                Request.Method.DELETE,
                StringResponse.class,
                store,
                URL_APPS,
                version.getAppUuid(),
                URL_VERSIONS,
                version.getUuid());

        request.addHeader("Content-Type", "application/json");

        return request;
    }
}
