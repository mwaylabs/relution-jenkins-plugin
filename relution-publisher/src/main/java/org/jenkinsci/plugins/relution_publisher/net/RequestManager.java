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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class RequestManager {

    /**
     * The maximum amount of time, in milliseconds, to wait for the connection manager to return
     * a connection from the connection pool.
     */
    private final static int     TIMEOUT_CONNECTION_REQUEST = 5000;

    /**
     * The connection attempt will time out if a connection cannot be established within the
     * specified amount of time, in milliseconds. 
     */
    private final static int     TIMEOUT_CONNECT            = 30000;

    /**
     * The connection will time out if the period of inactivity after receiving or sending a data
     * packet exceeds the specified value, in milliseconds. 
     */
    private final static int     TIMEOUT_SOCKET             = 10000;

    private final static Charset CHARSET                    = Charset.forName("UTF-8");

    private HttpHost             mProxyHost;

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

    public void setProxy(final String hostname, final int port) {
        this.mProxyHost = new HttpHost(hostname, port);
    }

    public HttpHost getProxy() {
        return this.mProxyHost;
    }

    public <T> ApiResponse<T> execute(final ApiRequest<T> request) throws InterruptedException, ExecutionException, IOException {

        final CloseableHttpAsyncClient client = this.createHttpClient();

        try {
            client.start();

            final Future<HttpResponse> future = request.execute(client);
            final HttpResponse httpResponse = future.get();

            return this.parseNetworkResponse(request, httpResponse);

        } finally {
            client.close();
        }
    }
}
