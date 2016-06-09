/*
 * Copyright (c) 2013-2015 M-Way Solutions GmbH
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

import com.google.gson.JsonObject;

import org.apache.http.nio.entity.NStringEntity;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.constants.ApiObject;
import org.jenkinsci.plugins.relution_publisher.constants.Headers;
import org.jenkinsci.plugins.relution_publisher.constants.Version;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest.Method;
import org.jenkinsci.plugins.relution_publisher.net.requests.BaseRequest;
import org.jenkinsci.plugins.relution_publisher.net.requests.EntityRequest;
import org.jenkinsci.plugins.relution_publisher.net.requests.ZeroCopyFileRequest;
import org.jenkinsci.plugins.relution_publisher.util.Json;
import org.jenkinsci.plugins.relution_publisher.util.UrlUtils;

import java.io.File;
import java.nio.charset.Charset;


/**
 * Provides static methods that can be used to create {@link ApiRequest}s that can be used to
 * communicate with the Relution Enterprise Appstore.
 */
public final class RequestFactory {

    private static final String  APPLICATION_JSON   = "application/json";
    private static final String  BASIC              = "Basic ";

    private final static Charset CHARSET            = Charset.forName("UTF-8");

    //
    // Relution core paths
    //
    /**
     * The URL used to authenticate the user and start a session.
     */
    private static final String  URL_AUTH_LOGIN     = "gofer/security/rest/auth/login";

    /**
     * The URL used to close an existing session.
     */
    private static final String  URL_AUTH_LOGOUT    = "gofer/security/rest/auth/logout";

    //
    // Relution paths
    //
    /**
     * The base API URL.
     */
    private final static String  URL_API_V1         = "relution/api/v1";

    /**
     * The URL used to request the languages configured on the server.
     */
    private final static String  URL_LANGUAGES      = URL_API_V1 + "/languages";

    /**
     * The URL used to request or persist application objects.
     */
    private final static String  URL_APPS           = URL_API_V1 + "/apps";

    /**
     * The URL used to request or persist asset objects.
     */
    private final static String  URL_FILES          = URL_API_V1 + "/files";

    /**
     * The URL used to request the unpersisted application object associated with a previously
     * uploaded asset.
     */
    private final static String  URL_APPS_FROM_FILE = URL_API_V1 + "/apps/fromFile";

    //
    // Path parts
    //
    /**
     * The path used to request or persist application version objects.
     */
    private final static String  VERSIONS           = "versions";

    private RequestFactory() {
    }

    private static String getUrl(final Store store, final String... parts) {
        final String baseUrl = UrlUtils.toBaseUrl(store.getUrl());
        final String path = UrlUtils.combine(parts);
        return UrlUtils.combine(baseUrl, path);
    }

    private static void addAuthentication(final BaseRequest request, final Store store) {

        request.setHeader(Headers.ACCEPT, APPLICATION_JSON);
        request.setHeader(Headers.AUTHORIZATION, BASIC + store.getAuthorizationToken());
    }

    /**
     * Creates a {@link EntityRequest} that can be used to authenticate the user against the server.
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to authenticate the user.
     */
    public static EntityRequest createLoginRequest(final Store store) {
        final EntityRequest request = new EntityRequest(
                Method.POST,
                getUrl(store, URL_AUTH_LOGIN));

        final JsonObject credentials = new JsonObject();
        credentials.addProperty("userName", store.getUsername());
        credentials.addProperty("password", store.getPassword());

        final NStringEntity entity = new NStringEntity(credentials.toString(), CHARSET);
        request.setEntity(entity);

        request.setHeader(Headers.CONTENT_TYPE, APPLICATION_JSON);
        return request;
    }

    /**
     * Creates a {@link EntityRequest} that can be used to close a session started with a login
     * request.
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to close a session.
     * @see #createLoginRequest(Store)
     */
    public static EntityRequest createLogoutRequest(final Store store) {
        final EntityRequest request = new EntityRequest(
                Method.POST,
                getUrl(store, URL_AUTH_LOGOUT));

        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to retrieve all language objects
     * stored in the server's settings.
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to query all languages on the server.
     */
    public static EntityRequest createLanguageRequest(final Store store) {

        final EntityRequest request = new EntityRequest(
                Method.GET,
                getUrl(store, URL_LANGUAGES));

        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to retrieve all application objects
     * stored in the server's database.
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to query all applications on the server.
     */
    public static EntityRequest createAppStoreItemsRequest(final Store store) {

        final EntityRequest request = new EntityRequest(
                Method.GET,
                getUrl(store, URL_APPS));

        request.queryFields().add("locale", "de");
        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to upload a {@link File} to the server.
     * @param store The {@link Store} this request should be executed against.
     * @param file The {@link File} to upload.
     * @return A request that can be used to upload a file to the server.
     */
    public static ZeroCopyFileRequest createUploadRequest(final Store store, final File file) {
        final ZeroCopyFileRequest request = new ZeroCopyFileRequest(
                getUrl(store, URL_FILES),
                file);

        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to retrieve the application associated
     * with the specified asset.
     * @param store The {@link Store} this request should be executed against.
     * @param asset The asset for which to retrieve the application.
     * @return A request that can be used to retrieved the application associated with an asset.
     */
    public static EntityRequest createAppFromFileRequest(final Store store, final JsonObject asset) {
        final String uuid = Json.getString(asset, ApiObject.UUID);

        final EntityRequest request = new EntityRequest(
                Method.POST,
                getUrl(store, URL_APPS_FROM_FILE, uuid));

        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to persist the specified application.
     * <p>
     * This call must not be used to persist an application that has already been persisted. Doing
     * so results in undefined behavior. An application is unpersisted if its identifier is
     * {@code null}.
     * @param store The {@link Store} this request should be executed against.
     * @param app The application to persist.
     * @return A request that can be used to persist the specified application.
     */
    public static EntityRequest createPersistApplicationRequest(final Store store, final JsonObject app) {
        final EntityRequest request = new EntityRequest(
                Method.POST,
                getUrl(store, URL_APPS));

        final NStringEntity entity = new NStringEntity(app.toString(), CHARSET);
        request.setEntity(entity);

        request.setHeader(Headers.CONTENT_TYPE, APPLICATION_JSON);
        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to persist the specified application version.
     * <p>
     * This call must not be used to persist a version that has already been persisted. Doing so
     * results in undefined behavior. A version is unpersisted if its
     * {@link ApiObject#UUID identifier} is {@code null}. The application associated with the
     * version must already be persisted.
     * @param store The {@link Store} this request should be executed against.
     * @param app The application to persist.
     * @param version The application version to persist.
     * @return A request that can be used to persist the specified version.
     */
    public static EntityRequest createPersistVersionRequest(final Store store, final JsonObject app, final JsonObject version) {
        final String appUuid = Json.getString(app, ApiObject.UUID);

        final EntityRequest request = new EntityRequest(
                Method.POST,
                getUrl(store, URL_APPS, appUuid, VERSIONS));

        final NStringEntity entity = new NStringEntity(version.toString(), CHARSET);
        request.setEntity(entity);

        request.setHeader(Headers.CONTENT_TYPE, APPLICATION_JSON);
        addAuthentication(request, store);
        return request;
    }

    public static EntityRequest createDeleteVersionRequest(final Store store, final JsonObject version) {
        final String appUuid = Json.getString(version, Version.APP_UUID);
        final String uuid = Json.getString(version, ApiObject.UUID);

        final EntityRequest request = new EntityRequest(
                Method.DELETE,
                getUrl(store, URL_APPS, appUuid, VERSIONS, uuid));

        addAuthentication(request, store);
        return request;
    }
}
