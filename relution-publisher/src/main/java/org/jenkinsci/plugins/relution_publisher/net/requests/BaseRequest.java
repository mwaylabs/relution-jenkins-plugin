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

package org.jenkinsci.plugins.relution_publisher.net.requests;

import org.apache.http.HttpRequest;
import org.apache.http.util.Args;
import org.jenkinsci.plugins.relution_publisher.net.RequestQueryFields;

import java.util.HashMap;
import java.util.Map;


/**
 * Basic implementation of an {@link ApiRequest}.
 * @param <T> The type of response entity returned by the request.
 */
public abstract class BaseRequest implements ApiRequest {

    private final Method mMethod;
    private final String mUri;

    private final Map<String, String> mHeaders     = new HashMap<String, String>();
    private final RequestQueryFields  mQueryFields = new RequestQueryFields();

    /**
     * Creates a new instance of the {@link BaseRequest} class.
     * @param method The request {@link Method} to be used for the request.
     * @param url The base URI that identifies the target of the request.
     * @param responseClass A class that identifies the type of entity that is expected to be
     * returned as a response of the request.
     */
    protected BaseRequest(final Method method, final String uri) {
        Args.notNull(method, "method");
        Args.notNull(uri, "uri");

        this.mMethod = method;
        this.mUri = uri;
    }

    protected void addHeaders(final HttpRequest request) {

        for (final String name : this.mHeaders.keySet()) {
            request.addHeader(name, this.mHeaders.get(name));
        }
    }

    /**
     * Adds a header with the specified name and value to the request. If a header with the same
     * name already exists it will be replaced.
     * @param name The name of the header to add.
     * @param value The value of the header to add.
     */
    public void setHeader(final String name, final String value) {
        this.mHeaders.put(name, value);
    }

    /**
     * Adds a header with the specified name and value to the request. If a header with the same
     * name already exists it will be replaced.
     * @param name The name of the header to add.
     * @param format The format string.
     * @param args The list of arguments passed to the formatter. If there are more arguments than
     * required by format, additional arguments are ignored.
     */
    public void setHeader(final String name, final String format, final Object... args) {
        final String value = String.format(format, args);
        this.mHeaders.put(name, value);
    }

    /**
     * Gets the query parameters to use for the request.
     * @return The {@link RequestQueryFields} to use.
     */
    public RequestQueryFields queryFields() {
        return this.mQueryFields;
    }

    /**
     * Gets the HTTP method to be used for this request.
     * @return The {@link Method} to use.
     */
    @Override
    public Method getMethod() {
        return this.mMethod;
    }

    /**
     * Gets the URI to use for the request, with optional query parameters appended.
     * <p/>
     * If no query parameters where specified the URI that is returned is identical to the URI that
     * was specified when this request was created; otherwise query parameters will be appended.
     * @return The URI that identifies the target of the request.
     */
    @Override
    public String getUri() {

        if (this.mQueryFields.size() == 0) {
            return this.mUri;
        }
        return this.mUri + this.mQueryFields.toString();
    }
}
