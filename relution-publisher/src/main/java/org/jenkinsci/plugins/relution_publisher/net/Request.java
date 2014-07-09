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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class Request<T> {

    /**
     * The maximum amount of time, in milliseconds, to wait for the connection manager to return
     * a connection from the connection pool.
     */
    private final static int                      TIMEOUT_CONNECTION_REQUEST = 5000;

    /**
     * The connection attempt will time out if a connection cannot be established within the
     * specified amount of time, in milliseconds. 
     */
    private final static int                      TIMEOUT_CONNECT            = 30000;

    /**
     * The connection will time out if the period of inactivity after receiving or sending a data
     * packet exceeds the specified value, in milliseconds. 
     */
    private final static int                      TIMEOUT_SOCKET             = 10000;

    private final static Charset                  CHARSET                    = Charset.forName("UTF-8");

    private final RequestQueryFields              mQueryFields               = new RequestQueryFields();

    private final Map<String, String>             mHeaders                   = new HashMap<String, String>();

    private HttpEntity                            mHttpEntity;
    private File                                  mFile;

    private final int                             mMethod;
    private final String                          mUrl;
    private final Class<? extends ApiResponse<T>> mResponseClass;

    private HttpHost                              mProxyHost;

    /**
     * Create an new Request Object
     * @param method 0: GET, 1: POST, 2: PUT, 3: DELETE
     * @param url specific url to which the request response
     */
    public Request(final int method, final String url, final Class<? extends ApiResponse<T>> responseClass) {

        this.mMethod = method;
        this.mUrl = url;
        this.mResponseClass = responseClass;
    }

    private String getUrl() {

        if (this.mQueryFields.size() == 0) {
            return this.mUrl;
        }
        return this.mUrl + this.mQueryFields.toString();
    }

    private HttpUriRequest createHttpRequest(final int method, final String uri, final HttpEntity entity) {

        switch (method) {
            default:
            case Method.GET:
                return new HttpGet(uri);

            case Method.POST:
                final HttpPost post = new HttpPost(uri);
                if (entity != null) {
                    post.setEntity(entity);
                }
                return post;

            case Method.PUT:
                final HttpPut put = new HttpPut(uri);
                if (entity != null) {
                    put.setEntity(entity);
                }
                return put;

            case Method.DELETE:
                return new HttpDelete(uri);
        }
    }

    private HttpUriRequest createHttpRequest() {

        final HttpUriRequest request = this.createHttpRequest(this.mMethod, this.getUrl(), this.mHttpEntity);

        for (final String name : this.mHeaders.keySet()) {
            request.addHeader(name, this.mHeaders.get(name));
        }

        return request;
    }

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

    private Future<HttpResponse> execute(final CloseableHttpAsyncClient client) throws URISyntaxException, FileNotFoundException {

        if (this.mMethod == Method.POST && this.mFile != null) {
            final FileRequest post = new FileRequest(this.getUrl(), this.mFile);
            post.addHeaders(this.mHeaders);

            final HttpAsyncResponseConsumer<HttpResponse> consumer = new BasicAsyncResponseConsumer();
            return client.execute(post, consumer, null);
        }

        final HttpUriRequest httpRequest = this.createHttpRequest();
        return client.execute(httpRequest, null);
    }

    private ApiResponse<T> getJsonString(final HttpResponse httpResponse) {

        String payload = null;

        try {
            final HttpEntity entity = httpResponse.getEntity();
            payload = EntityUtils.toString(entity, CHARSET);

            return ApiResponse.fromJson(payload, this.mResponseClass);

        } catch (final Exception e) {
            e.printStackTrace();
        }
        final ApiResponse<T> response = new ApiResponse<T>();
        response.setMessage(payload);
        return response;
    }

    private ApiResponse<T> parseNetworkResponse(final HttpResponse httpResponse) {

        final ApiResponse<T> response = this.getJsonString(httpResponse);
        response.init(httpResponse);

        return response;
    }

    public void setProxy(final String hostname, final int port) {
        this.mProxyHost = new HttpHost(hostname, port);
    }

    public void addHeader(final String name, final String value) {
        this.mHeaders.put(name, value);
    }

    public void addHeader(final String name, final String format, final Object... args) {
        final String value = String.format(format, args);
        this.mHeaders.put(name, value);
    }

    public RequestQueryFields queryFields() {
        return this.mQueryFields;
    }

    public HttpEntity getEntity() {
        return this.mHttpEntity;
    }

    public void setEntity(final HttpEntity entity) {
        this.mHttpEntity = entity;
    }

    public void setFile(final File file) {
        this.mFile = file;
    }

    public File getFile() {
        return this.mFile;
    }

    public ApiResponse<T> execute() throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        final CloseableHttpAsyncClient client = this.createHttpClient();

        try {
            client.start();

            final Future<HttpResponse> future = this.execute(client);
            final HttpResponse httpResponse = future.get();

            return this.parseNetworkResponse(httpResponse);

        } finally {
            client.close();
        }
    }

    /**
     * Supported request methods.
     */
    public interface Method {

        public final static int GET    = 0;
        public final static int POST   = 1;
        public final static int PUT    = 2;
        public final static int DELETE = 3;
    }
}