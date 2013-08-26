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

package org.jenkinsci.plugins.relution_publisher.net.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jenkinsci.plugins.relution_publisher.entities.Error;


/**
 * Represents a response returned by the Relution server.
 */
public abstract class ApiResponse {

    public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final int        status;
    private final String     message;

    private final Error      errors;

    private final int        total;

    private transient String s;

    /**
     * Converts the specified JSON formatted string to an instance of the specified class. 
     * @param json A JSON formatted string.
     * @param clazz A {@link Class} that extends {@link ApiResponse}.
     * @return An instance of the specified class.
     */
    public static <T extends ApiResponse> T fromJson(final String json, final Class<T> clazz) {
        return ApiResponse.GSON.fromJson(json, clazz);
    }

    /**
     * Initializes a new instance of the {@link ApiResponse} class.
     */
    protected ApiResponse() {

        this.status = 0;
        this.message = null;

        this.errors = null;

        this.total = 0;
    }

    /**
     * Gets the status code returned by the server.
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Gets the message returned by the server.
     */
    public String getMessage() {
        return this.message;
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
     * Gets a JSON formatted String that represents this instance.
     */
    public String toJson() {
        return ApiResponse.GSON.toJson(this);
    }

    @Override
    public String toString() {

        if (this.s == null) {
            this.s = this.toJson();
        }
        return this.s;
    }
}
