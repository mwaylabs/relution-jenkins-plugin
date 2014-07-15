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

package org.jenkinsci.plugins.relution_publisher.net.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.jenkinsci.plugins.relution_publisher.entities.Error;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a response returned by the Relution server.
 */
public class ApiResponse<T> {

    private final static Gson GSON    = new GsonBuilder().setPrettyPrinting().create();

    private int               statusCode;
    private String            reason;

    private final Integer     status;
    private String            message;

    private final Error       errors;

    private final int         total;
    private final List<T>     results = new ArrayList<T>();

    private transient String  s;

    /**
     * Converts the specified JSON formatted string to an instance of the specified class. 
     * @param json A JSON formatted string.
     * @param clazz A {@link Class} that extends {@link ApiResponse}.
     * @return An instance of the specified class.
     */
    public static <T> ApiResponse<T> fromJson(final String json, final Class<? extends ApiResponse<T>> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Initializes a new instance of the {@link ApiResponse} class.
     */
    public ApiResponse() {

        this.status = null;

        this.message = null;
        this.errors = null;

        this.total = 0;
    }

    /**
     * Initializes the response from the specified {@link HttpResponse}.
     * @param httpResponse A {@link HttpResponse} used to initialize internal fields.
     */
    public void init(final HttpResponse httpResponse) {

        this.statusCode = httpResponse.getStatusLine().getStatusCode();
        this.reason = httpResponse.getStatusLine().getReasonPhrase();
    }

    /**
     * Gets the HTTP status code returned by the server.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the status returned by the server.
     */
    public int getStatus() {

        if (this.status == null) {
            return this.statusCode;
        }
        return this.status;
    }

    /**
     * Gets the message returned by the server.
     */
    public String getMessage() {

        if (StringUtils.isBlank(this.message)) {
            return this.reason;
        }
        return this.message;
    }

    /**
     * Sets the message returned by the server.
     * @param message The message to set.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Gets the {@link Error} returned by the server, or <code>null</null> if no error occurred.
     */
    public Error getError() {
        return this.errors;
    }

    /**
     * Gets the total number of results returned by the server.
     */
    public int getCount() {
        return this.total;
    }

    /**
     * Gets the results returned by the server.
     */
    public List<T> getResults() {
        return this.results;
    }

    /**
     * Gets a JSON formatted String that represents this instance.
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {

        if (this.s == null) {
            this.s = this.toJson();
        }
        return this.s;
    }
}
