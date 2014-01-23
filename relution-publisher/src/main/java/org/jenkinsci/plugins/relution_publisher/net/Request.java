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
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.relution_publisher.entities.ApiObject;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class Request<T extends ApiObject> {

    private final static Charset                  CHARSET      = Charset.forName("UTF-8");

    private final RequestQueryFields              mQueryFields = new RequestQueryFields();

    private final Map<String, String>             mHeaders     = new HashMap<String, String>();
    private HttpEntity                            mHttpEntity;

    private final int                             mMethod;
    private final String                          mUrl;
    private final Class<? extends ApiResponse<T>> mResponseClass;

    private BasicCookieStore                      mCookieStore;
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

    private HttpRequestBase createHttpRequest(final int method, final HttpEntity entity) {

        switch (method) {
            default:
            case Method.GET:
                return new HttpGet();

            case Method.POST:
                final HttpPost post = new HttpPost();
                if (entity != null) {
                    post.setEntity(entity);
                }
                return post;

            case Method.PUT:
                final HttpPut put = new HttpPut();
                if (entity != null) {
                    put.setEntity(entity);
                }
                return put;

            case Method.DELETE:
                return new HttpDelete();
        }
    }

    private HttpRequestBase createHttpRequest() throws URISyntaxException {
        final HttpRequestBase request = this.createHttpRequest(this.mMethod, this.mHttpEntity);

        for (final String name : this.mHeaders.keySet()) {
            request.addHeader(name, this.mHeaders.get(name));
        }
        final URI uri = new URI(this.getUrl());
        request.setURI(uri);

        return request;
    }

    private String getUrl() {

        if (this.mQueryFields.size() == 0) {
            return this.mUrl;
        }
        return this.mUrl + this.mQueryFields.toString();
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

    public HttpEntity entity() {
        return this.mHttpEntity;
    }

    public void setEntity(final HttpEntity entity) {
        this.mHttpEntity = entity;
    }

    public ApiResponse<T> execute() throws URISyntaxException, ClientProtocolException, IOException {

        final DefaultHttpClient client = new DefaultHttpClient();

        try {
            if (this.mProxyHost != null) {
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, this.mProxyHost);
            }
            client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            client.setCookieStore(this.mCookieStore);

            final HttpRequestBase httpRequest = this.createHttpRequest();
            final HttpResponse httpResponse = client.execute(httpRequest);

            return this.parseNetworkResponse(httpResponse);

        } finally {
            client.getConnectionManager().shutdown();
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