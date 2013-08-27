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
import org.jenkinsci.plugins.relution_publisher.net.Response;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApplicationResponse;
import org.jenkinsci.plugins.relution_publisher.net.responses.UploadResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class ArtifactFileUploader implements FileCallable<Boolean> {

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
    public Boolean invoke(final File basePath, final VirtualChannel channel) throws IOException, InterruptedException {

        try {
            this.log.write(this, "Uploading build artifact...");
            final List<Response<UploadResponse>> responses = this.uploadAsset(basePath, this.publication.getArtifactPath());

            if (this.isEmpty(responses)) {
                this.log.write(this, "No artifact to upload found.");
                this.build.setResult(Result.NOT_BUILT);
                return true;
            }

            for (final Response<UploadResponse> response : responses) {
                this.uploadVersion(basePath, response);
            }

        } catch (final URISyntaxException e) {
            this.log.write(this, "Publication failed with %s", e.toString());
            this.build.setResult(Result.UNSTABLE);
        }
        return true;
    }

    private void uploadVersion(final File basePath, final Response<UploadResponse> response)
            throws ClientProtocolException, URISyntaxException, IOException {

        if (!this.verifyUploadResponse(response)) {
            this.log.write(this, "Upload of the build artifact failed.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final UploadResponse data = response.getData();
        final List<Asset> assets = data.getAssets();

        if (assets.size() != 1) {
            this.log.write(this, "More than one unpersisted asset object returned by server.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final Asset asset = assets.get(0);
        this.log.write(this, "Upload completed, received upload token %s", asset.getUuid());

        this.retrieveApplication(basePath, asset);
    }

    private void retrieveApplication(final File basePath, final Asset asset)
            throws ClientProtocolException, URISyntaxException, IOException {

        this.log.write(this, "Retrieving application associated with file '%s'...", asset.getUuid());
        final Request<ApplicationResponse> request = RequestFactory.createAppFromFileRequest(this.store, asset.getUuid());
        final Response<ApplicationResponse> response = request.execute();

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Retrieval of the application failed.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final ApplicationResponse data = response.getData();
        final Application app = this.getApplication(data, asset);

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

        this.setVersionName(version);
        this.setVersionIcon(basePath, version);

        this.setChangeLog(basePath, version);
        this.setDescription(basePath, version);
    }

    private void setReleaseStatus(final Version version) {

        if (StringUtils.isBlank(this.publication.getReleaseStatus())) {
            return;
        }

        version.setReleaseStatus(this.publication.getReleaseStatus());
    }

    private void setVersionName(final Version version) {

        if (StringUtils.isBlank(this.publication.getVersionName())) {
            this.log.write(this, "No name set, default name will be used.");
            return;
        }

        version.setVersionName(this.publication.getVersionName());
    }

    private void setVersionIcon(final File basePath, final Version version)
            throws ClientProtocolException, URISyntaxException, IOException {

        if (StringUtils.isBlank(this.publication.getIconPath())) {
            this.log.write(this, "No icon set, default icon will be used.");
            return;
        }

        this.log.write(this, "Uploading application icon...");
        final String filePath = this.publication.getIconPath();
        final List<Response<UploadResponse>> responses = this.uploadAsset(basePath, filePath);

        if (this.isEmpty(responses)) {
            this.log.write(this, "Failed to upload application icon.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final Response<UploadResponse> response = responses.get(0);

        if (!this.verifyUploadResponse(response)) {
            this.log.write(this, "Failed to upload application icon.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        final UploadResponse data = response.getData();

        version.setIcon(data.getAssets().get(0));
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

    private void persistApplication(final Application app)
            throws ClientProtocolException, URISyntaxException, IOException {

        this.log.write(this, "Application is new, persisting application...");

        final Request<ApplicationResponse> request = RequestFactory.createPersistApplicationRequest(this.store, app);
        final Response<ApplicationResponse> response = request.execute();

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

        final Request<ApplicationResponse> request = RequestFactory.createPersistVersionRequest(this.store, version);
        final Response<ApplicationResponse> response = request.execute();

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting version.");
            this.build.setResult(Result.UNSTABLE);
            return;
        }

        this.log.write(this, "Version persisted successfully.");
    }

    private List<Response<UploadResponse>> uploadAsset(final File basePath, final String filePath, final String uploadToken)
            throws ClientProtocolException, URISyntaxException, IOException {

        final FileSet fileSet = Util.createFileSet(basePath, filePath);
        final File directory = fileSet.getDirectoryScanner().getBasedir();

        if (fileSet.getDirectoryScanner().getIncludedFilesCount() < 1) {
            this.log.write(this, "The file specified by '%s' does not exist, upload failed.", filePath);
            return null;
        }

        final List<Response<UploadResponse>> responses = new ArrayList<Response<UploadResponse>>();

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            final File file = new File(directory, fileName);

            this.log.write(this, "Uploading file '%s'...", fileName);
            final Request<UploadResponse> request = RequestFactory.createUploadRequest(this.store, file, uploadToken);
            responses.add(request.execute());
            this.log.write(this, "Upload of file completed.");
        }

        return responses;
    }

    private List<Response<UploadResponse>> uploadAsset(final File basePath, final String filePath)
            throws ClientProtocolException, URISyntaxException, IOException {

        return this.uploadAsset(basePath, filePath, "");
    }

    private Application getApplication(final ApplicationResponse response, final Asset asset) {

        for (final Application app : response.getApplications()) {
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
        return sb.toString();
    }

    private void readFile(final File file, final StringBuilder sb) {

        try {
            final BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
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

    private boolean verifyUploadResponse(final Response<UploadResponse> response) {

        final UploadResponse data = response.getData();

        if (data == null) {
            this.log.write(this, "Error uploading file (%d): %s", response.getStatusCode(), response.getRawData());
            return false;
        }

        if (data.getStatus() != 0) {
            this.log.write(this, "Error uploading file (%d): %s", response.getStatusCode(), data.getMessage());
            return false;
        }

        if (this.isEmpty(data.getAssets())) {
            this.log.write(this, "Error uploading file, the server returned no asset objects.");
            return false;
        }

        return true;
    }

    private boolean verifyApplicationResponse(final Response<ApplicationResponse> response) {

        final ApplicationResponse data = response.getData();

        if (data == null) {
            this.log.write(this, "Error creating application object (%d): %s", response.getStatusCode(), response.getRawData());
            return false;
        }

        if (data.getStatus() != 0) {
            this.log.write(this, "Error creating application object (%d): %s", response.getStatusCode(), data.getMessage());
            return false;
        }

        if (this.isEmpty(data.getApplications())) {
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
