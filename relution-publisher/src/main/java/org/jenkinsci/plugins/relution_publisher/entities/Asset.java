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

package org.jenkinsci.plugins.relution_publisher.entities;

/**
 * Represents an asset associated with a {@link Version}. An asset can either be the executable
 * file represented by a version or additional files associated with a version (e.g. images, videos
 * or other binary files).
 */
public class Asset {

    private final String  uuid;

    private final String  name;
    private final String  link;
    private final String  contentType;

    private final long    size;
    private final Long    modificationDate;

    private final Integer downloadCount;

    protected Asset() {

        this.uuid = null;

        this.name = null;
        this.link = null;
        this.contentType = null;

        this.size = 0;
        this.modificationDate = null;

        this.downloadCount = null;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getLink() {
        return this.link;
    }

    public String getContentType() {
        return this.contentType;
    }

    public long getSize() {
        return this.size;
    }

    public Long getModificationDate() {
        return this.modificationDate;
    }

    public Integer getDownloadCount() {
        return this.downloadCount;
    }
}
