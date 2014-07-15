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

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a category associated with a {@link Version}.
 */
public class Category extends ApiObject {

    private final Map<String, String> name        = new HashMap<String, String>();
    private final Map<String, String> description = new HashMap<String, String>();

    public Map<String, String> getName() {
        return this.name;
    }

    public Map<String, String> getDescription() {
        return this.description;
    }
}
