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

package org.jenkinsci.plugins.relution_publisher.net.requests;

import org.apache.http.HttpResponse;
import org.apache.http.nio.client.HttpAsyncClient;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.IOException;
import java.util.concurrent.Future;


/**
 * Represents an API request against a Relution Enterprise Appstore.
 * @param <T> The type of response entity returned by the request.
 */
public interface ApiRequest<T> {

    Future<HttpResponse> execute(HttpAsyncClient httpClient) throws IOException;

    Class<? extends ApiResponse<T>> getResponseType();

    Method getMethod();

    String getUri();

    /**
     * Identifies the HTTP method to use for a request.
     */
    public static enum Method {
        /**
         * The GET method means retrieve whatever information (in the form of an entity) is identified
         * by the Request-URI.
         */
        GET,
        /**
         * The POST method is used to request that the origin server accept the entity enclosed in the
         * request as a new subordinate of the resource identified by the Request-URI in the
         * Request-Line. 
         */
        POST,
        /**
         * The PUT method requests that the enclosed entity be stored under the supplied Request-URI.
         */
        PUT,
        /**
         * The DELETE method requests that the origin server delete the resource identified by the
         * Request-URI.
         */
        DELETE
    }
}
