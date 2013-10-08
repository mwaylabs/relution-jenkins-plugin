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

package org.jenkinsci.plugins.relution_publisher.builder;

import hudson.FilePath.FileCallable;
import hudson.Util;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;
import org.jenkinsci.plugins.relution_publisher.entities.Application;
import org.jenkinsci.plugins.relution_publisher.entities.Asset;
import org.jenkinsci.plugins.relution_publisher.entities.Version;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.net.Request;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * Uploads build artifacts to a {@link Store} that has been specified in a Jenkins project's
 * post-build action, in the form of a {@link Publication}. 
 */
public class ArtifactFileUploader implements FileCallable<Boolean> {

    private static final long         serialVersionUID = 1L;

    private final AbstractBuild<?, ?> build;

    private final Publication         publication;
    private final Store               store;
    private final Log                 log;

    /**
     * Initializes a new instance of the {@link ArtifactFileUploader} class.
     * @param build The build that produced the artifact to be published.
     * @param publication The {@link Publication} that describes the artifact to be published.
     * @param store The {@link Store} to which the publication should be published.
     * @param log The {@link Log} to write log messages to.
     */
    public ArtifactFileUploader(final AbstractBuild<?, ?> build, final Publication publication, final Store store, final Log log) {

        this.build = build;

        this.publication = publication;
        this.store = store;
        this.log = log;
    }

    @Override
    public Boolean invoke(final File basePath, final VirtualChannel channel)
            throws IOException, InterruptedException {

        try {
            this.log.write(this, "Uploading build artifact...");
            final List<ApiResponse<Asset>> responses = this.uploadAsset(basePath, this.publication.getArtifactPath());

            if (this.isEmpty(responses)) {
                this.log.write(this, "No artifact to upload found.");
                this.build.setResult(Result.NOT_BUILT);
                return true;
            }

            for (final ApiResponse<Asset> response : responses) {
                this.uploadVersion(basePath, response);
            }

        } catch (final URISyntaxException e) {
            this.log.write(this, "Publication failed with %s", e.toString());
            this.build.setResult(Result.UNSTABLE);
        }
        return true;
    }

    private void uploadVersion(final File basePath, final ApiResponse<Asset> response)
            throws ClientProtocolException, URISyntaxException, IOException {

        if (!this.verifyAssetResponse(response)) {
            this.log.write(this, "Upload of the build artifact failed.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final List<Asset> assets = response.getResults();

        if (assets.size() != 1) {
            this.log.write(this, "More than one unpersisted asset object returned by server.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final Asset asset = assets.get(0);
        this.log.write(this, "Upload completed, received token %s", asset.getUuid());

        this.retrieveApplication(basePath, asset);
    }

    private void retrieveApplication(final File basePath, final Asset asset)
            throws ClientProtocolException, URISyntaxException, IOException {

        this.log.write(this, "Requesting application object associated with token '%s'...", asset.getUuid());
        final Request<Application> request = RequestFactory.createAppFromFileRequest(this.store, asset);
        final ApiResponse<Application> response = request.execute();

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Retrieval of the application object failed.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final List<Application> applications = response.getResults();
        final Application app = this.getApplication(applications, asset);

        if (app == null) {
            this.build.setResult(Result.UNSTABLE);
            this.log.write(this, "Could not find application object associated with the file.");
            return;
        }

        final Version version = this.getVersion(app, asset);

        if (version == null) {
            this.log.write(this, "Could not find version object associated with the file.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        this.setVersionMetadata(basePath, version);

        if (app.getUuid() == null) {
            this.persistApplication(app);
        } else {
            version.setAppUuid(app.getUuid());
            this.persistVersion(version);
        }
    }

    private void setVersionMetadata(final File basePath, final Version version)
            throws ClientProtocolException, URISyntaxException, IOException {

        this.setReleaseStatus(version);

        this.setName(version);
        this.setIcon(basePath, version);

        this.setChangeLog(basePath, version);
        this.setDescription(basePath, version);

        this.setVersionName(version);
    }

    private void setReleaseStatus(final Version version) {

        final String key = !this.publication.usesDefaultReleaseStatus()
                ? this.publication.getReleaseStatus()
                : this.store.getReleaseStatus();

        if (!StringUtils.isBlank(key)) {
            version.setReleaseStatus(key);
        }
    }

    private void setName(final Version version) {

        if (StringUtils.isBlank(this.publication.getName())) {
            this.log.write(this, "No name set, default name will be used.");
            return;
        }

        for (final String key : version.getName().keySet()) {
            version.getName().put(key, this.publication.getName());
        }
    }

    private void setIcon(final File basePath, final Version version)
            throws ClientProtocolException, URISyntaxException, IOException {

        if (StringUtils.isBlank(this.publication.getIconPath())) {
            this.log.write(this, "No icon set, default icon will be used.");
            return;
        }

        this.log.write(this, "Uploading application icon...");
        final String filePath = this.publication.getIconPath();
        final List<ApiResponse<Asset>> responses = this.uploadAsset(basePath, filePath);

        if (this.isEmpty(responses)) {
            this.log.write(this, "Failed to upload application icon.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final ApiResponse<Asset> response = responses.get(0);

        if (!this.verifyAssetResponse(response)) {
            this.log.write(this, "Failed to upload application icon.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final List<Asset> assets = response.getResults();

        if (assets.size() != 1) {
            this.log.write(this, "More than one unpersisted asset object returned by server.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        version.setIcon(assets.get(0));
    }

    private void setChangeLog(final File basePath, final Version version) {

        if (StringUtils.isBlank(this.publication.getChangeLogPath())) {
            this.log.write(this, "No change log set.");
            return;
        }

        final String filePath = this.publication.getChangeLogPath();
        final String changeLog = this.readFile(basePath, filePath);

        if (!StringUtils.isBlank(changeLog)) {
            final String text = this.getEllipsizedText(changeLog.replace("\n", "<br/>"), 50);
            this.log.write(this, "Set change log to: '%s'", text);

            for (final String key : version.getChangelog().keySet()) {
                version.getChangelog().put(key, changeLog);
            }
        }
    }

    private void setDescription(final File basePath, final Version version) {

        if (StringUtils.isBlank(this.publication.getDescriptionPath())) {
            this.log.write(this, "No description set.");
            return;
        }

        final String filePath = this.publication.getDescriptionPath();
        final String description = this.readFile(basePath, filePath);

        if (!StringUtils.isBlank(description)) {
            final String text = this.getEllipsizedText(description.replace("\n", "<br/>"), 50);
            this.log.write(this, "Set change log to: '%s'", text);

            for (final String key : version.getDescription().keySet()) {
                version.getDescription().put(key, description);
            }
        }
    }

    private void setVersionName(final Version version) {

        if (StringUtils.isBlank(this.publication.getVersionName())) {
            this.log.write(this, "No version name set, default name will be used.");
            return;
        }

        version.setVersionName(this.publication.getVersionName());
    }

    private void persistApplication(final Application app)
            throws ClientProtocolException, URISyntaxException, IOException {

        this.log.write(this, "Application is new, persisting application...");

        final Request<Application> request = RequestFactory.createPersistApplicationRequest(this.store, app);
        final ApiResponse<Application> response = request.execute();

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting application.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        this.log.write(this, "Application persisted successfully.");
    }

    private void persistVersion(final Version version)
            throws ClientProtocolException, URISyntaxException, IOException {

        this.log.write(this, "Version is new, persisting version...");

        final Request<Application> request = RequestFactory.createPersistVersionRequest(this.store, version);
        final ApiResponse<Application> response = request.execute();

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting version.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        this.log.write(this, "Version persisted successfully.");
    }

    private List<ApiResponse<Asset>> uploadAsset(final File basePath, final String filePath)
            throws ClientProtocolException, URISyntaxException, IOException {

        if (StringUtils.isBlank(filePath)) {
            this.log.write(this, "No file to upload specified, filter expression is empty, upload failed.");
            return null;
        }

        final FileSet fileSet = Util.createFileSet(basePath, filePath);
        final File directory = fileSet.getDirectoryScanner().getBasedir();

        if (fileSet.getDirectoryScanner().getIncludedFilesCount() < 1) {
            this.log.write(this, "The file specified by '%s' does not exist, upload failed.", filePath);
            return null;
        }

        final List<ApiResponse<Asset>> responses = new ArrayList<ApiResponse<Asset>>();

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            final File file = new File(directory, fileName);

            this.log.write(this, "Uploading file '%s'...", fileName);
            final Request<Asset> request = RequestFactory.createUploadRequest(this.store, file);
            responses.add(request.execute());
            this.log.write(this, "Upload of file completed.");
        }

        return responses;
    }

    private Application getApplication(final List<Application> applications, final Asset asset) {

        for (final Application app : applications) {
            for (final Version version : app.getVersions()) {
                if (version.getFile() != null && StringUtils.equals(asset.getUuid(), version.getFile().getUuid())) {
                    return app;
                }
            }
        }
        return null;
    }

    private String readFile(final File basePath, final String filePath) {

        final FileSet fileSet = Util.createFileSet(basePath, filePath);
        final File directory = fileSet.getDirectoryScanner().getBasedir();
        final StringBuilder sb = new StringBuilder();

        if (fileSet.getDirectoryScanner().getIncludedFilesCount() < 1) {
            this.log.write(this, "The file specified by '%s' does not exist.", filePath);
        }

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            this.log.write(this, "Reading file '%s'...", fileName);
            final File file = new File(directory, fileName);
            this.readFile(file, sb);
        }
        return this.getEllipsizedText(sb.toString(), 49152);
    }

    private void readFile(final File file, final StringBuilder sb) {

        try {
            final BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null && sb.length() < 49152) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private Version getVersion(final Application app, final Asset asset) {

        for (final Version version : app.getVersions()) {
            if (version.getFile() != null && StringUtils.equals(asset.getUuid(), version.getFile().getUuid())) {
                return version;
            }
        }
        return null;
    }

    private boolean verifyAssetResponse(final ApiResponse<Asset> response) {

        final List<Asset> assets = response.getResults();

        if (response.getStatus() != 0) {
            this.log.write(this, "Error uploading file (%d), server's response:\n%s", response.getStatusCode(), response.getMessage());
            return false;
        }

        if (this.isEmpty(assets)) {
            this.log.write(this, "Error uploading file, the server returned no asset objects.");
            return false;
        }

        return true;
    }

    private boolean verifyApplicationResponse(final ApiResponse<Application> response) {

        final List<Application> applications = response.getResults();

        if (response.getStatus() != 0) {
            this.log.write(this, "Error creating application object (%d), server's response:\n%s", response.getStatusCode(), response.getMessage());
            return false;
        }

        if (this.isEmpty(applications)) {
            this.log.write(this, "Error creating application object, the server returned no application objects.");
            return false;
        }

        return true;
    }

    private String getEllipsizedText(final String input, final int maxLen) {

        if (input.length() <= maxLen) {
            return input;
        }

        final String output = input.substring(0, maxLen - 3) + "...";
        return output;
    }

    private boolean isEmpty(final List<?> list) {
        return (list == null || list.size() == 0);
    }
}
