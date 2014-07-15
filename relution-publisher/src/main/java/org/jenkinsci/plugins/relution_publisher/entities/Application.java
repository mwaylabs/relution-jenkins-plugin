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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents an application available in the Relution Enterprise Appstore. An application
 * has at least one {@link Version} associated with it.
 */
public class Application extends ApiObject {

    private final String                    type;
    private final String                    internalName;

    private final List<String>              platforms  = new ArrayList<String>();
    private final List<Category>            categories = new ArrayList<Category>();

    private final List<Version>             versions   = new ArrayList<Version>();

    private final Float                     rating;
    private final Integer                   ratingCount;
    private final Integer                   downloadCount;

    private final Map<String, List<String>> acl        = new HashMap<String, List<String>>();

    private final String                    createdBy;
    private final Long                      creationDate;
    private final String                    modifiedBy;
    private final Long                      modificationDate;

    protected Application() {

        this.type = null;
        this.internalName = null;

        this.rating = null;
        this.ratingCount = null;
        this.downloadCount = null;

        this.createdBy = null;
        this.creationDate = null;
        this.modifiedBy = null;
        this.modificationDate = null;
    }

    public String getType() {
        return this.type;
    }

    public String getInternalName() {
        return this.internalName;
    }

    public List<String> getPlatforms() {
        return this.platforms;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    public List<Version> getVersions() {
        return this.versions;
    }

    public Float getRating() {
        return this.rating;
    }

    public Integer getRatingCount() {
        return this.ratingCount;
    }

    public Integer getDownloadCount() {
        return this.downloadCount;
    }

    public Map<String, List<String>> getAcl() {
        return this.acl;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public Long getCreationDate() {
        return this.creationDate;
    }

    public String getModifiedBy() {
        return this.modifiedBy;
    }

    public Long getModificationDate() {
        return this.modificationDate;
    }
}
