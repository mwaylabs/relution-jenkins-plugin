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

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.global.StoreConfiguration;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.constants.UploadMode;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;


/**
 * Represents a publication configured in a Jenkins project. A publication determines which
 * Relution Enterprise Appstore an artifact should be uploaded to.
 */
public class Publication extends AbstractDescribableImpl<Publication> implements Serializable {

    /**
     * The serial version number of this class.
     * <p>
     * This version number is used to determine whether a serialized representation of this class
     * is compatible with the current implementation of the class.
     * <p>
     * <b>Note</b> Maintainers must change this value <b>if and only if</b> the new version of this
     * class is not compatible with old versions.
     * @see
     * <a href="http://docs.oracle.com/javase/6/docs/platform/serialization/spec/version.html">
     * Versioning of Serializable Objects</a>.
     */
    private static final long serialVersionUID = 1L;

    private String            artifactPath;
    private String            artifactExcludePath;
    private String            storeId;

    private String            releaseStatus;
    private String            archiveMode;
    private String            uploadMode;

    private String            name;
    private String            iconPath;
    private String            changeLogPath;
    private String            descriptionPath;
    private String            versionName;

    @DataBoundConstructor
    public Publication(
            final String artifactPath,
            final String artifactExcludePath,
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
        this.setArtifactExcludePath(artifactExcludePath);
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
     * @return The path of the artifact to be published, relative to the workspace directory.
     */
    public String getArtifactPath() {
        return this.artifactPath;
    }

    /**
     * Sets the path of the artifact to be published, relative to the workspace directory.
     * @param includes The pattern that defines which artifacts to include.
     */
    public void setArtifactPath(final String includes) {
        this.artifactPath = includes;
    }

    /**
     * @return The exclude pattern for the artifact to be published, relative to the workspace
     * directory.
     */
    public String getArtifactExcludePath() {
        return this.artifactExcludePath;
    }

    /**
     * Sets the exclude pattern for the artifact to be published, relative to the workspace
     * directory.
     * @param excludes The pattern that defines which artifacts to exclude.
     */
    public void setArtifactExcludePath(final String excludes) {
        this.artifactExcludePath = excludes;
    }

    /**
     * @return The identifier of the {@link Store} to which the artifact should be published.
     */
    public String getStoreId() {
        return this.storeId;
    }

    /**
     * Sets the identifier of the {@link Store} to which the artifact should be publishes.
     * @param storeId The {@link Store#getId() identifier} of a store.
     */
    public void setStoreId(final String storeId) {
        this.storeId = storeId;
    }

    /**
     * @return The key of the {@link ReleaseStatus} to use when uploading an artifact to the store.
     */
    public String getReleaseStatus() {
        return this.releaseStatus;
    }

    /**
     * Gets a value indicating whether the {@link ReleaseStatus} of the publication equals the
     * default value.
     * @return {@code true} if the release status is equal to {@link ReleaseStatus#DEFAULT};
     * otherwise, {@code false}.
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
     * @return The key of the {@link ArchiveMode} to use when uploading an artifact to the store.
     */
    public String getArchiveMode() {
        return this.archiveMode;
    }

    /**
     * Gets a value indicating whether the {@link ArchiveMode} of the publication equals the
     * default value.
     * @return {@code true} if the archive mode is equal to {@link ArchiveMode#DEFAULT};
     * otherwise, {@code false}.
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
     * @return The key of the {@link UploadMode} that determines which artifacts to upload to the
     * store.
     */
    public String getUploadMode() {
        return this.uploadMode;
    }

    /**
     * Gets a value indicating whether the {@link UploadMode} of the publication equals the
     * default value.
     * @return {@code true} if the upload mode is equal to {@link UploadMode#DEFAULT};
     * otherwise, {@code false}.
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
     * @return The name to show for the application version uploaded to the store.
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
     * @return The path of the icon to use for the version when uploading the artifact to the store.
     */
    public String getIconPath() {
        return this.iconPath;
    }

    /**
     * Sets the path of the icon to use for the version when uploading the artifact to the store.
     * @param iconPath The path of the icon to use, or {@code null} to use the icon specified by
     * the artifact's meta-data.
     */
    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * @return The path of the file that contains the version's change log.
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
     * @return The path of the file that contains the version's description.
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
     * @return The name to use for the version when uploading the artifact to the store.
     */
    public String getVersionName() {
        return this.versionName;
    }

    /**
     * Sets the name to use for the version when uploading the artifact to the store.
     * <p>
     * This is not the application's name, but its version name, i.e. the human readable version.
     * @param versionName The version name to use, or {@code null} to use the version name
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

        public FormValidation doCheckStoreId(@QueryParameter final String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Please select a store from the list");
            }
            return FormValidation.ok();
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

        public ListBoxModel doFillStoreIdItems() {

            final List<Store> stores = this.globalConfiguration.getStores();
            final ListBoxModel items = new ListBoxModel();
            items.add("", "");

            for (final Store store : stores) {
                items.add(store.toString(), store.getId());
            }

            return items;
        }

        public ListBoxModel doFillReleaseStatusItems(@QueryParameter final String storeId) {
            final Store store = this.globalConfiguration.getStore(storeId);

            final ListBoxModel items = new ListBoxModel();
            ReleaseStatus.fillList(items, store);
            return items;
        }

        public ListBoxModel doFillArchiveModeItems(@QueryParameter final String storeId) {
            final Store store = this.globalConfiguration.getStore(storeId);

            final ListBoxModel items = new ListBoxModel();
            ArchiveMode.fillListBox(items, store);
            return items;
        }

        public ListBoxModel doFillUploadModeItems(@QueryParameter final String storeId) {
            final Store store = this.globalConfiguration.getStore(storeId);

            final ListBoxModel items = new ListBoxModel();
            UploadMode.fillListBox(items, store);
            return items;
        }
    }
}
