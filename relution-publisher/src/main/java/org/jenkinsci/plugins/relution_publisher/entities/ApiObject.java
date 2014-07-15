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

package org.jenkinsci.plugins.relution_publisher.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Represents an API object returned by the Relution server.
 */
public abstract class ApiObject {

    private final static Gson GSON = new GsonBuilder().create();

    private final String      uuid;

    private transient String  s;

    protected ApiObject() {
        this.uuid = null;
    }

    public String getUuid() {
        return this.uuid;
    }

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
