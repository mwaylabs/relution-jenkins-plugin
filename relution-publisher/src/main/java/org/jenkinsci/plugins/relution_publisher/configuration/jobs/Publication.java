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

package org.jenkinsci.plugins.relution_publisher.configuration.jobs;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.global.StoreConfiguration;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.constants.UploadMode;
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
    private String archiveMode;
    private String uploadMode;

    private String name;
    private String iconPath;
    private String changeLogPath;
    private String descriptionPath;
    private String versionName;

    @DataBoundConstructor
    public Publication(
            final String artifactPath,
            final String storeId,
            final String releaseStatus,
            final String archiveMode,
            final String uploadMode,
            final String name,
            final String iconPath,
            final String changeLogPath,
            final String descriptionPath,
            final String versionName) {

        this.setArtifactPath(artifactPath);
        this.setStoreId(storeId);
        this.setReleaseStatus(releaseStatus);
        this.setArchiveMode(archiveMode);
        this.setUploadMode(uploadMode);
        this.setName(name);
        this.setIconPath(iconPath);
        this.setChangeLogPath(changeLogPath);
        this.setDescriptionPath(descriptionPath);
        this.setVersionName(versionName);
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
     * Gets a value indicating whether the {@link ReleaseStatus} of the publication equals the
     * default value.
     * @return <code>true</code> if the release status is equal to {@link ReleaseStatus#DEFAULT};
     * otherwise, <code>false</code>.
     */
    public boolean usesDefaultReleaseStatus() {
        return StringUtils.isBlank(this.releaseStatus) || this.releaseStatus.equals(ReleaseStatus.DEFAULT.key);
    }

    /**
     * Sets the key of the {@link ReleaseStatus} to use when uploading an artifact to the store.
     * @param releaseStatus The release status to use.
     */
    public void setReleaseStatus(final String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    /**
     * Gets the key of the {@link ArchiveMode} to use when uploading an artifact to the store.
     */
    public String getArchiveMode() {
        return this.archiveMode;
    }

    /**
     * Gets a value indicating whether the {@link ArchiveMode} of the publication equals the
     * default value.
     * @return <code>true</code> if the archive mode is equal to {@link ArchiveMode#DEFAULT};
     * otherwise, <code>false</code>.
     */
    public boolean usesDefaultArchiveMode() {
        return StringUtils.isBlank(this.archiveMode) || this.archiveMode.equals(ArchiveMode.DEFAULT.key);
    }

    /**
     * Sets the key of the {@link ArchiveMode} to use when uploading an artifact to the store.
     * @param archiveMode The archive mode to use.
     */
    public void setArchiveMode(final String archiveMode) {
        this.archiveMode = archiveMode;
    }

    /**
     * Gets the key of the {@link UploadMode} that determines which artifacts to upload to the
     * store.
     */
    public String getUploadMode() {
        return this.uploadMode;
    }

    /**
     * Gets a value indicating whether the {@link UploadMode} of the publication equals the
     * default value.
     * @return <code>true</code> if the upload mode is equal to {@link UploadMode#DEFAULT};
     * otherwise, <code>false</code>.
     */
    public boolean usesDefaultUploadMode() {
        return StringUtils.isBlank(this.uploadMode) || this.uploadMode.equals(UploadMode.DEFAULT.key);
    }

    /**
     * Sets the key of the {@link UploadMode} that determines which artifacts to upload to the
     * store.
     * @param uploadMode The upload mode to use.
     */
    public void setUploadMode(final String uploadMode) {
        this.uploadMode = uploadMode;
    }

    /**
     * Gets the name to show for the application version uploaded to the store.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name to show for the application version uploaded to the store.
     * @param name The name to show.
     */
    public void setName(final String name) {
        this.name = name;
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

    /**
     * Gets the name to use for the version when uploading the artifact to the store.
     */
    public String getVersionName() {
        return this.versionName;
    }

    /**
     * Sets the name to use for the version when uploading the artifact to the store.
     * <p/>
     * This is not the application's name, but its version name, i.e. the human readable version.
     * @param versionName The version name to use, or <code>null</code> to use the version name
     * specified by the artifact's meta-data.
     */
    public void setVersionName(final String versionName) {
        this.versionName = versionName;
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

        public FormValidation doCheckVersionName(@QueryParameter final String value) {

            if (!StringUtils.isBlank(value)) {
                return FormValidation.warning("You are overwriting the human readable version, are you sure this is what you want?");
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

        public FormValidation doCheckReleaseStatus(@QueryParameter final String value) {

            if (StringUtils.equals(value, ReleaseStatus.REVIEW.key)) {
                return FormValidation.ok("User account may require additional permissions to upload to \"Review\".");
            }

            if (StringUtils.equals(value, ReleaseStatus.RELEASE.key)) {
                return FormValidation.ok("User account may require additional permissions to upload to \"Release\".");
            }

            return FormValidation.ok();
        }

        public ListBoxModel doFillReleaseStatusItems() {

            final ListBoxModel items = new ListBoxModel();
            ReleaseStatus.fillListBoxWithDefault(items);
            return items;
        }

        public ListBoxModel doFillArchiveModeItems() {
            final ListBoxModel items = new ListBoxModel();
            ArchiveMode.fillListBoxWithDefault(items);
            return items;
        }

        public ListBoxModel doFillUploadModeItems() {
            final ListBoxModel items = new ListBoxModel();
            UploadMode.fillListBoxWithDefault(items);
            return items;
        }
    }
}
