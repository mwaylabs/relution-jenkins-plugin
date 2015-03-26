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
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.util.ErrorType;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class RequestManager implements Serializable {

    /**
     * The serial version number of this class.
     * <p>
     * This version number is used to determine whether a serialized representation of this class
     * is compatible with the current implementation of the class.
     * <p>
     * <b>Note</b> Maintainers must change this value <b>if and only if</b> the new version of this
     * class is not compatible with old versions.
     * @see
     * <a href="http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html">
     * Versioning of Serializable Objects</a>.
     */
    private static final long                  serialVersionUID           = 1L;

    /**
     * The maximum amount of time, in milliseconds, to wait for the connection manager to return
     * a connection from the connection pool.
     */
    private final static int                   TIMEOUT_CONNECTION_REQUEST = 10000;

    /**
     * The connection attempt will time out if a connection cannot be established within the
     * specified amount of time, in milliseconds.
     */
    private final static int                   TIMEOUT_CONNECT            = 60000;

    /**
     * The connection will time out if the period of inactivity after receiving or sending a data
     * packet exceeds the specified value, in milliseconds.
     */
    private final static int                   TIMEOUT_SOCKET             = 60000;

    /**
     * The maximum number of times a request is retried in case a time out occurs.
     */
    private final static int                   MAX_REQUEST_RETRIES        = 3;

    private final static Charset               CHARSET                    = Charset.forName("UTF-8");

    private transient CloseableHttpAsyncClient mHttpClient;
    private HttpHost                           mProxyHost;

    private CloseableHttpAsyncClient createHttpClient() {

        final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(TIMEOUT_CONNECTION_REQUEST);
        requestConfigBuilder.setConnectTimeout(TIMEOUT_CONNECT);
        requestConfigBuilder.setSocketTimeout(TIMEOUT_SOCKET);

        if (this.mProxyHost != null) {
            requestConfigBuilder.setProxy(this.mProxyHost);
        }

        final HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();
        final RequestConfig requestConfig = requestConfigBuilder.build();

        clientBuilder.setDefaultRequestConfig(requestConfig);

        return clientBuilder.build();
    }

    private CloseableHttpAsyncClient getHttpClient() {

        if (this.mHttpClient == null) {
            this.mHttpClient = this.createHttpClient();
        }
        return this.mHttpClient;
    }

    private HttpResponse send(final ApiRequest<?> request, final Log log) throws IOException, InterruptedException, ExecutionException {

        final CloseableHttpAsyncClient client = this.getHttpClient();
        int retries = MAX_REQUEST_RETRIES;

        if (!client.isRunning()) {
            client.start();
        }

        while (true) {
            try {
                final Future<HttpResponse> future = request.execute(client);
                return future.get();

            } catch (final ExecutionException e) {
                retries = this.attemptRetryOnException(e, retries, log);
            }
        }
    }

    private int attemptRetryOnException(final ExecutionException e, final int retries, final Log log) throws ExecutionException {
        final int remainingRetries = retries - 1;

        if (remainingRetries <= 0) {
            this.log(log, "Maximum number of retries, giving up");
            throw e;
        }

        if (ErrorType.is(e, ExecutionException.class, ConnectTimeoutException.class)) {
            this.log(log, "Timeout while attempting to connect to the server, retrying...");
            return remainingRetries;

        } else if (ErrorType.is(e, ExecutionException.class, SocketTimeoutException.class)) {
            this.log(log, "Timeout while sending or receiving data, retrying...");
            return remainingRetries;

        } else if (ErrorType.is(e, ExecutionException.class, SocketException.class)) {
            this.log(log, "Error creating network socket, retrying...");
            return remainingRetries;

        }

        throw e;
    }

    private <T> ApiResponse<T> getJsonString(final ApiRequest<T> request, final HttpResponse httpResponse) {

        String payload = null;

        try {
            final HttpEntity entity = httpResponse.getEntity();
            payload = EntityUtils.toString(entity, CHARSET);

            return ApiResponse.fromJson(payload, request.getResponseType());

        } catch (final Exception e) {
            e.printStackTrace();
        }
        final ApiResponse<T> response = new ApiResponse<T>();
        response.setMessage(payload);
        return response;
    }

    /**
     * Parses the specified network response.
     * @param httpResponse The {@link HttpResponse} to parse.
     * @return An {@link ApiResponse} constructed from the contents of the specified response.
     */
    private <T> ApiResponse<T> parseNetworkResponse(final ApiRequest<T> request, final HttpResponse httpResponse) {

        final ApiResponse<T> response = this.getJsonString(request, httpResponse);
        response.init(httpResponse);
        return response;
    }

    private void log(final Log log, final String format, final Object... args) {

        if (log == null) {
            return;
        }
        log.write(this, format, args);
    }

    public void setProxy(final String hostname, final int port) {

        if (!StringUtils.isBlank(hostname) && port != 0) {
            this.mProxyHost = new HttpHost(hostname, port);
        }
    }

    public HttpHost getProxy() {
        return this.mProxyHost;
    }

    public <T> ApiResponse<T> execute(final ApiRequest<T> request, final Log log) throws IOException, InterruptedException, ExecutionException {

        final HttpResponse httpResponse = this.send(request, log);
        return this.parseNetworkResponse(request, httpResponse);
    }

    public <T> ApiResponse<T> execute(final ApiRequest<T> request) throws InterruptedException, ExecutionException, IOException {
        return this.execute(request, null);
    }

    public void shutdown() throws IOException {

        if (this.mHttpClient != null) {
            this.mHttpClient.close();
        }
    }
}
