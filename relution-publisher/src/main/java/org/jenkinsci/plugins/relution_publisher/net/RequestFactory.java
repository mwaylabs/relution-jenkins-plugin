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

package org.jenkinsci.plugins.relution_publisher.net;

import org.apache.commons.lang.StringUtils;
import org.apache.http.nio.entity.NStringEntity;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.entities.Application;
import org.jenkinsci.plugins.relution_publisher.entities.Asset;
import org.jenkinsci.plugins.relution_publisher.entities.Language;
import org.jenkinsci.plugins.relution_publisher.entities.Version;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest.Method;
import org.jenkinsci.plugins.relution_publisher.net.requests.BaseRequest;
import org.jenkinsci.plugins.relution_publisher.net.requests.EntityRequest;
import org.jenkinsci.plugins.relution_publisher.net.requests.ZeroCopyFileRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApplicationResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.AssetResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.LanguageResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.StringResponse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;


/**
 * Provides static methods that can be used to create {@link ApiRequest}s that can be used to
 * communicate with the Relution Enterprise Appstore.
 */
public final class RequestFactory {

    private static final String HEADER_ACCEPT        = "Accept";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private final static String HEADER_CONTENT_TYPE  = "Content-Type";

    private static final String APPLICATION_JSON = "application/json";
    private static final String BASIC            = "Basic ";

    private final static Charset CHARSET = Charset.forName("UTF-8");

    /**
     * The URL used to request the languages configured on the server.
     */
    private final static String URL_LANGUAGES = "languages";

    /**
     * The URL used to request or persist {@link Application} objects.
     */
    private final static String URL_APPS = "apps";

    /**
     * The URL used to request or persist {@link Version} objects.
     */
    private final static String URL_VERSIONS = "versions";

    /**
     * The URL used to request or persist {@link Asset} objects.
     */
    private final static String URL_FILES = "files";

    /**
     * The URL used to request the unpersisted {@link Application} object associated with a
     * previously uploaded {@link Asset}.
     */
    private final static String URL_APPS_FROM_FILE = "apps/fromFile";

    private RequestFactory() {
    }

    public static String sanitizePath(final String path) {

        if (StringUtils.isBlank(path)) {
            return path;
        }

        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static String getUrl(final Store store, final String... parts) {

        final StringBuilder sb = new StringBuilder();
        sb.append(sanitizePath(store.getUrl()));

        for (final String part : parts) {
            if (!part.startsWith("/")) {
                sb.append("/");
            }
            final String path = sanitizePath(part);
            sb.append(path);
        }
        return sb.toString();
    }

    private static void addAuthentication(final BaseRequest<?> request, final Store store) {

        request.setHeader(HEADER_ACCEPT, APPLICATION_JSON);
        request.setHeader(HEADER_AUTHORIZATION, BASIC + store.getAuthorizationToken());
    }

    /**
     * Creates a {@link BaseRequest} that can be used to retrieve all {@link Language} objects
     * stored in the server's settings.
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to query all languages on the server.
     */
    public static EntityRequest<Language> createLanguageRequest(final Store store) {

        final EntityRequest<Language> request = new EntityRequest<Language>(
                Method.GET,
                getUrl(store, URL_LANGUAGES),
                LanguageResponse.class);

        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to retrieve all {@link Application} objects
     * stored in the server's database.
     * @param store The {@link Store} this request should be executed against.
     * @return A request that can be used to query all applications on the server.
     * @throws URISyntaxException
     */
    public static EntityRequest<Application> createAppStoreItemsRequest(final Store store) {

        final EntityRequest<Application> request = new EntityRequest<Application>(
                Method.GET,
                getUrl(store, URL_APPS),
                ApplicationResponse.class);

        request.queryFields().add("locale", "de");
        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to upload a {@link File} to the server.
     * @param store The {@link Store} this request should be executed against.
     * @param file The {@link File} to upload.
     * @return A request that can be used to upload a file to the server.
     * @throws IOException The specified file could not be buffered for sending.
     */
    public static ZeroCopyFileRequest<Asset> createUploadRequest(final Store store, final File file) {

        final ZeroCopyFileRequest<Asset> request = new ZeroCopyFileRequest<Asset>(
                getUrl(store, URL_FILES),
                file,
                AssetResponse.class);

        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to retrieve the {@link Application} associated
     * with the specified {@link Asset}.
     * @param store The {@link Store} this request should be executed against.
     * @param asset The {@link Asset} for which to retrieve the {@link Application}
     * @return A request that can be used to retrieved the application associated with an asset.
     */
    public static EntityRequest<Application> createAppFromFileRequest(final Store store, final Asset asset) {

        final EntityRequest<Application> request = new EntityRequest<Application>(
                Method.POST,
                getUrl(store, URL_APPS_FROM_FILE, asset.getUuid()),
                ApplicationResponse.class);

        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to persist the specified {@link Application}.
     * <p/>
     * This call must not be used to persist an application that has already been persisted. Doing
     * so results in undefined behavior. An application is unpersisted if its
     * {@link Application#getUuid() identifier} is <code>null</code>.
     * @param store The {@link Store} this request should be executed against.
     * @param app The {@link Application} to persist.
     * @return A request that can be used to persist the specified application.
     */
    public static EntityRequest<Application> createPersistApplicationRequest(final Store store, final Application app) {

        final EntityRequest<Application> request = new EntityRequest<Application>(
                Method.POST,
                getUrl(store, URL_APPS),
                ApplicationResponse.class);

        final NStringEntity entity = new NStringEntity(app.toJson(), CHARSET);
        request.setEntity(entity);

        request.setHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON);
        addAuthentication(request, store);
        return request;
    }

    /**
     * Creates a {@link BaseRequest} that can be used to persist the specified {@link Version}.
     * <p/>
     * This call must not be used to persist a version that has already been persisted. Doing so
     * results in undefined behavior. A version is unpersisted if its
     * {@link Version#getUuid() identifier} is <code>null</code>. The {@link Application}
     * associated with the version must already be persisted.
     * @param store The {@link Store} this request should be executed against.
     * @param version The {@link Version} to persist.
     * @return A request that can be used to persist the specified version.
     */
    public static EntityRequest<Application> createPersistVersionRequest(final Store store, final Version version) {

        final EntityRequest<Application> request = new EntityRequest<Application>(
                Method.POST,
                getUrl(store, URL_APPS, version.getAppUuid(), URL_VERSIONS),
                ApplicationResponse.class);

        final NStringEntity entity = new NStringEntity(version.toJson(), CHARSET);
        request.setEntity(entity);

        request.setHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON);
        addAuthentication(request, store);
        return request;
    }

    public static EntityRequest<String> createDeleteVersionRequest(final Store store, final Version version) {

        final EntityRequest<String> request = new EntityRequest<String>(
                Method.DELETE,
                getUrl(store, URL_APPS, version.getAppUuid(), URL_VERSIONS, version.getUuid()),
                StringResponse.class);

        addAuthentication(request, store);
        return request;
    }
}
