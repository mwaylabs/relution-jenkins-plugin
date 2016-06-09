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

package org.jenkinsci.plugins.relution_publisher.net.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;


/**
 * Represents a response returned by the Relution server.
 */
public class ApiResponse {

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private HttpResponse      httpResponse;
    private int               statusCode;
    private String            reason;

    @Expose
    @SerializedName("status")
    private final Integer     status;

    @Expose
    @SerializedName("message")
    private String            message;

    @Expose
    @SerializedName("errors")
    private final JsonObject  errors;

    @Expose
    @SerializedName("total")
    private final int         total;

    @Expose
    @SerializedName("results")
    private final JsonArray   results;

    private transient String  s;

    /**
     * Converts the specified JSON formatted string to an instance of the specified class.
     * @param json A JSON formatted string.
     * @return An instance of the specified class.
     */
    public static ApiResponse fromJson(final String json) {
        final Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        return gson.fromJson(json, ApiResponse.class);
    }

    /**
     * Initializes a new instance of the {@link ApiResponse} class.
     */
    public ApiResponse() {
        this.status = null;

        this.message = null;
        this.errors = new JsonObject();

        this.total = 0;
        this.results = new JsonArray();
    }

    /**
     * Returns the underlying HTTP response.
     * @return The {@link HttpResponse} from which this API response was parsed.
     */
    public HttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    /**
     * Initializes the response from the specified {@link HttpResponse}.
     * @param httpResponse A {@link HttpResponse} used to initialize internal fields.
     */
    public void setHttpResponse(final HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
        this.statusCode = httpResponse.getStatusLine().getStatusCode();
        this.reason = httpResponse.getStatusLine().getReasonPhrase();
    }

    /**
     * @return The HTTP status code returned by the server.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * @return The status returned by the server.
     */
    public int getStatus() {

        if (this.status == null) {
            return this.statusCode;
        }
        return this.status;
    }

    /**
     * @return The message returned by the server.
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
     * @return The {@link Error} returned by the server, or {@code null} if no error occurred.
     */
    public JsonObject getError() {
        return this.errors;
    }

    /**
     * @return The total number of results returned by the server.
     */
    public int getCount() {
        return this.total;
    }

    /**
     * @return The results returned by the server.
     */
    public JsonArray getResults() {
        return this.results;
    }

    /**
     * @return A JSON formatted String that represents this instance.
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
