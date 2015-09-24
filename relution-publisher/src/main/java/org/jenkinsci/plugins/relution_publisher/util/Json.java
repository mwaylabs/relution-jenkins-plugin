/*
 * Copyright (c) 2015 M-Way Solutions GmbH
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

package org.jenkinsci.plugins.relution_publisher.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class Json {

    public static boolean isNull(final JsonObject object, final String memberName) {
        final JsonElement element = object.get(memberName);
        return element == null || element.isJsonNull();
    }

    public static JsonObject getObject(final JsonObject object, final String memberName) {
        final JsonElement element = object.get(memberName);

        if (element == null || !element.isJsonObject()) {
            return null;
        }

        return element.getAsJsonObject();
    }

    public static JsonArray getArray(final JsonObject object, final String memberName) {
        final JsonElement element = object.get(memberName);

        if (element == null || !element.isJsonArray()) {
            return new JsonArray();
        }

        return element.getAsJsonArray();
    }

    public static String getString(final JsonObject object, final String memberName) {
        final JsonElement element = object.get(memberName);

        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }

        return element.getAsString();
    }

    public static int getInt(final JsonObject object, final String memberName) {
        final JsonElement element = object.get(memberName);

        if (element == null || !element.isJsonPrimitive()) {
            return 0;
        }

        return element.getAsInt();
    }

    public static Integer getInteger(final JsonObject object, final String memberName) {
        final JsonElement element = object.get(memberName);

        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }

        return element.getAsInt();
    }

    public static JsonObject getObject(final JsonArray array, final int index) {
        final JsonElement element = array.get(index);

        if (element == null || !element.isJsonObject()) {
            return null;
        }

        return element.getAsJsonObject();
    }
}
