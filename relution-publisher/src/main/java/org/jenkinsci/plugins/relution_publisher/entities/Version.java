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
 * Represents a distinct version of an {@link Application} that is available for download
 * in the Relution Enterprise Appstore.
 */
public class Version extends ApiObject {

    private String                    appUuid;

    private String                    versionName;
    private final int                 versionCode;

    private String                    releaseStatus;

    private final Integer             downloadCount;
    private final Integer             installCount;

    private final String              link;

    private final Asset               file;
    private Asset                     icon;
    private final List<Asset>         screenshots = new ArrayList<Asset>();

    private final List<Constraint>    constraints = new ArrayList<Constraint>();

    private final Map<String, String> name        = new HashMap<String, String>();
    private final Map<String, String> keywords    = new HashMap<String, String>();

    private final Map<String, String> description = new HashMap<String, String>();
    private final Map<String, String> changelog   = new HashMap<String, String>();

    private final String              copyright;
    private final String              developerName;
    private final String              developerWeb;
    private final String              developerEmail;

    private final String              createdBy;
    private final Long                creationDate;

    private final String              modifiedBy;
    private final Long                modificationDate;

    protected Version() {

        this.appUuid = null;

        this.releaseStatus = null;
        this.versionName = null;
        this.versionCode = 0;

        this.downloadCount = null;
        this.installCount = null;

        this.link = null;

        this.file = null;
        this.icon = null;

        this.copyright = null;
        this.developerName = null;
        this.developerWeb = null;
        this.developerEmail = null;

        this.createdBy = null;
        this.creationDate = null;

        this.modifiedBy = null;
        this.modificationDate = null;
    }

    public String getAppUuid() {
        return this.appUuid;
    }

    public void setAppUuid(final String appUuid) {
        this.appUuid = appUuid;
    }

    public Integer getDownloadCount() {
        return this.downloadCount;
    }

    public String getReleaseStatus() {
        return this.releaseStatus;
    }

    public void setReleaseStatus(final String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public Integer getInstallCount() {
        return this.installCount;
    }

    public String getLink() {
        return this.link;
    }

    public Asset getFile() {
        return this.file;
    }

    public Asset getIcon() {
        return this.icon;
    }

    public void setIcon(final Asset icon) {
        this.icon = icon;
    }

    public List<Asset> getScreenshots() {
        return this.screenshots;
    }

    public List<Constraint> getConstraints() {
        return this.constraints;
    }

    public Map<String, String> getName() {
        return this.name;
    }

    public Map<String, String> getKeywords() {
        return this.keywords;
    }

    public Map<String, String> getDescription() {
        return this.description;
    }

    public Map<String, String> getChangelog() {
        return this.changelog;
    }

    public String getCopyright() {
        return this.copyright;
    }

    public String getDeveloperName() {
        return this.developerName;
    }

    public String getDeveloperWeb() {
        return this.developerWeb;
    }

    public String getDeveloperEmail() {
        return this.developerEmail;
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
