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

package org.jenkinsci.plugins.relution_publisher.configuration.jobs;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.global.StoreConfiguration;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;

import javax.inject.Inject;


/**
 * Represents a publication configured in a Jenkins project. A publication determines which
 * Relution Enterprise Appstore an artifact should be uploaded to.
 */
public class Publication extends AbstractDescribableImpl<Publication> {

    private String artifactPath;
    private String storeId;

    private String releaseStatus;

    private String versionName;
    private String iconPath;
    private String changeLogPath;
    private String descriptionPath;

    @DataBoundConstructor
    public Publication(
            final String artifactPath,
            final String storeId,
            final String releaseStatus,
            final String versionName,
            final String iconPath,
            final String changeLogPath,
            final String descriptionPath) {

        this.setArtifactPath(artifactPath);
        this.setStoreId(storeId);
        this.setReleaseStatus(releaseStatus);
        this.setVersionName(versionName);
        this.setIconPath(iconPath);
        this.setChangeLogPath(changeLogPath);
        this.setDescriptionPath(descriptionPath);
    }

    /**
     * Gets the path of the artifact to be published, relative to the workspace directory.
     */
    public String getArtifactPath() {
        return this.artifactPath;
    }

    /**
     * Sets the path of the artifact to be published, relative to the workspace directory.
     * @param filePath The path of the artifact to be published.
     */
    public void setArtifactPath(final String filePath) {
        this.artifactPath = filePath;
    }

    /**
     * Gets the identifier of the {@link Store} to which the artifact should be published. 
     */
    public String getStoreId() {
        return this.storeId;
    }

    /**
     * Sets the identifier of the {@link Store} to which the artifact should be publishes.
     * @param storeId The {@link Store#getIdentifier() identifier} of a store.
     */
    public void setStoreId(final String storeId) {
        this.storeId = storeId;
    }

    /**
     * Gets the key of the {@link ReleaseStatus} to use when uploading an artifact to the store.
     */
    public String getReleaseStatus() {
        return this.releaseStatus;
    }

    /**
     * Sets the key of the {@link ReleaseStatus} to use when uploading an artifact to the store.
     * @param releaseStatus The release status to use.
     */
    public void setReleaseStatus(final String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    /**
     * Gets the name to use for the version when uploading the artifact to the store.
     */
    public String getVersionName() {
        return this.versionName;
    }

    /**
     * Sets the name to use for the version when uploading the artifact to the store.
     * @param versionName The name to use, or <code>null</code> to use the name specified by
     * the artifact's meta-data.
     */
    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    /**
     * Gets the path of the icon to use for the version when uploading the artifact to the store.
     */
    public String getIconPath() {
        return this.iconPath;
    }

    /**
     * Sets the path of the icon to use for the version when uploading the artifact to the store.
     * @param iconPath The path of the icon to use, or <code>null</code> to use the icon specified
     * by the artifact's meta-data.
     */
    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * Gets the path of the file that contains the version's change log.
     */
    public String getChangeLogPath() {
        return this.changeLogPath;
    }

    /**
     * Sets the path of the file that contains the version's change log.
     * @param changeLogPath The path to use for the change log.
     */
    public void setChangeLogPath(final String changeLogPath) {
        this.changeLogPath = changeLogPath;
    }

    /**
     * Gets the path of the file that contains the version's description.
     */
    public String getDescriptionPath() {
        return this.descriptionPath;
    }

    /**
     * Sets the path of the file that contains the version's description.
     * @param descriptionPath The path to use for the description.
     */
    public void setDescriptionPath(final String descriptionPath) {
        this.descriptionPath = descriptionPath;
    }

    @Extension
    public static class PublicationDescriptor extends Descriptor<Publication> {

        @Inject
        private StoreConfiguration globalConfiguration;

        @Override
        public String getDisplayName() {
            return "Publication";
        }

        public FormValidation doCheckArtifactPath(@QueryParameter final String value) {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Path must not be empty");
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillStoreIdItems() {

            final List<Store> stores = this.globalConfiguration.getStores();
            final ListBoxModel items = new ListBoxModel();

            for (final Store store : stores) {
                items.add(store.toString(), store.getIdentifier());
            }

            return items;
        }

        public ListBoxModel doFillReleaseStatusItems() {

            final ListBoxModel items = new ListBoxModel();
            ReleaseStatus.fillListBox(items);
            return items;
        }
    }
}
