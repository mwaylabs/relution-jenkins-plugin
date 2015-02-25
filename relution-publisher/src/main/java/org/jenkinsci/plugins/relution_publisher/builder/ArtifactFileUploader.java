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

package org.jenkinsci.plugins.relution_publisher.builder;

import com.google.common.base.Stopwatch;

import hudson.FilePath.FileCallable;
import hudson.Util;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.entities.Application;
import org.jenkinsci.plugins.relution_publisher.entities.Asset;
import org.jenkinsci.plugins.relution_publisher.entities.Version;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.RequestManager;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.util.Builds;
import org.jenkinsci.remoting.RoleChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Uploads build artifacts to a {@link Store} that has been specified in a Jenkins project's
 * post-build action, in the form of a {@link Publication}.
 */
public class ArtifactFileUploader implements FileCallable<Boolean> {

    private static final long         serialVersionUID = 1L;
    private static final int          MAX_TEXT_LENGTH  = 49152;

    private final AbstractBuild<?, ?> build;

    private final Publication         publication;
    private final Store               store;
    private final Log                 log;

    private final RequestManager      requestManager;

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

        this.requestManager = new RequestManager();
        this.requestManager.setProxy(store.getProxyHost(), store.getProxyPort());
    }

    @Override
    public Boolean invoke(final File basePath, final VirtualChannel channel)
            throws IOException, InterruptedException {

        try {
            this.log.write(this, "Uploading build artifacts...");
            final List<ApiResponse<Asset>> responses = this.uploadAssets(
                    basePath,
                    this.publication.getArtifactPath(),
                    this.publication.getArtifactExcludePath());

            if (this.isEmpty(responses) && this.build.getResult() == Result.UNSTABLE) {
                this.log.write(this, "Upload of build artifacts failed.");
                return true;

            } else if (this.isEmpty(responses)) {
                this.log.write(this, "No artifacts to upload found.");
                Builds.set(this.build, Result.NOT_BUILT, this.log);
                return true;
            }

            for (final ApiResponse<Asset> response : responses) {
                this.uploadVersion(basePath, response);
            }

        } catch (final IOException e) {
            this.log.write(this, "Publication failed.\n\n%s\n", e);
            Builds.set(this.build, Result.UNSTABLE, this.log);

        } catch (final URISyntaxException e) {
            this.log.write(this, "Publication failed.\n\n%s\n", e);
            Builds.set(this.build, Result.UNSTABLE, this.log);

        } catch (final ExecutionException e) {
            this.log.write(this, "Publication failed.\n\n%s\n", e);
            Builds.set(this.build, Result.UNSTABLE, this.log);

        } finally {
            this.requestManager.shutdown();

        }

        return true;
    }

    private void uploadVersion(final File basePath, final ApiResponse<Asset> response)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        if (!this.verifyAssetResponse(response)) {
            this.log.write(this, "Upload of build artifacts failed.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        final List<Asset> assets = response.getResults();

        if (assets.size() != 1) {
            this.log.write(this, "More than one unpersisted asset returned by server.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        final Asset asset = assets.get(0);
        this.log.write(this, "Upload completed, received token {%s}", asset.getUuid());

        this.retrieveApplication(basePath, asset);
    }

    private void retrieveApplication(final File basePath, final Asset asset)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        this.log.write(this, "Requesting application associated with token {%s}...", asset.getUuid());
        final ApiRequest<Application> request = RequestFactory.createAppFromFileRequest(this.store, asset);
        final ApiResponse<Application> response = this.requestManager.execute(request, this.log);

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Retrieval of application failed.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        final List<Application> applications = response.getResults();
        final Application app = this.getApplication(applications, asset);

        if (app == null) {
            Builds.set(this.build, Result.UNSTABLE, this.log);
            this.log.write(this, "Could not find application associated with uploaded file.");
            return;
        }

        this.log.write(this, "Application \"%s\" was retrieved.", app.getInternalName());
        this.log.write(this, "Searching version associated with uploaded file...");
        final Version version = this.getVersion(app, asset);

        if (version == null) {
            this.log.write(this, "Could not find version associated with uploaded file.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        this.log.write(this, "Found version \"%s\".", version.getVersionName());
        this.setVersionMetadata(basePath, version);

        if (app.getUuid() == null) {
            this.persistApplication(app);

        } else {
            final List<Version> archived = this.getArchivedVersions(app, version);

            version.setAppUuid(app.getUuid());
            this.persistVersion(version);

            this.manageArchivedVersions(archived, version);
        }

        this.log.write(
                this,
                "Uploaded version \"%s\" (%d) to \"%s\"",
                version.getVersionName(),
                version.getVersionCode(),
                version.getReleaseStatus());
    }

    private List<Version> getArchivedVersions(final Application app, final Version version) {

        final List<Version> archived = new ArrayList<Version>();

        for (final Version current : app.getVersions()) {
            if (StringUtils.equals(current.getReleaseStatus(), version.getReleaseStatus()) && current.getVersionCode() != version.getVersionCode()) {
                archived.add(current);
            }
        }

        return archived;
    }

    private void manageArchivedVersions(final List<Version> archived, final Version version)
            throws URISyntaxException, InterruptedException, ExecutionException {

        final String key = !this.publication.usesDefaultArchiveMode()
                ? this.publication.getArchiveMode()
                : this.store.getArchiveMode();

        if (StringUtils.equals(key, ArchiveMode.OVERWRITE.key)) {
            this.log.write(this, "Delete previous application version from \"%s\"", version.getReleaseStatus());

            for (final Version current : archived) {
                this.deleteVersion(current);
            }

        } else {
            this.log.write(this, "Keep previous application version (moved to archive)");

        }
    }

    private void deleteVersion(final Version version) throws URISyntaxException, InterruptedException, ExecutionException {

        this.log.write(
                this,
                "Deleting version \"%s\" (%d) from \"%s\"...",
                version.getVersionName(),
                version.getVersionCode(),
                version.getReleaseStatus());

        try {
            final ApiRequest<String> request = RequestFactory.createDeleteVersionRequest(this.store, version);
            final ApiResponse<String> response = this.requestManager.execute(request, this.log);

            if (!this.verifyDeleteResponse(response)) {
                this.log.write(this, "Error deleting version");
                Builds.set(this.build, Result.UNSTABLE, this.log);
                return;
            }

        } catch (final IOException e) {
            this.log.write(this, "Error deleting version: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setVersionMetadata(final File basePath, final Version version)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

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
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        if (StringUtils.isBlank(this.publication.getIconPath())) {
            this.log.write(this, "No icon set, default icon will be used.");
            return;
        }

        this.log.write(this, "Uploading application icon...");
        final String filePath = this.publication.getIconPath();
        final List<ApiResponse<Asset>> responses = this.uploadAssets(basePath, filePath, null);

        if (this.isEmpty(responses)) {
            this.log.write(this, "Failed to upload application icon.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        final ApiResponse<Asset> response = responses.get(0);

        if (!this.verifyAssetResponse(response)) {
            this.log.write(this, "Failed to upload application icon.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        final List<Asset> assets = response.getResults();

        if (assets.size() != 1) {
            this.log.write(this, "More than one unpersisted asset object returned by server.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
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
            this.log.write(this, "Set change log to: \"%s\" (%d characters)", text, changeLog.length());

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
            this.log.write(this, "Set change log to: \"%s\" (%d characters)", text, description.length());

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
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        this.log.write(this, "Application is new, persisting application...");

        final ApiRequest<Application> request = RequestFactory.createPersistApplicationRequest(this.store, app);
        final ApiResponse<Application> response = this.requestManager.execute(request, this.log);

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting application.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        this.log.write(this, "Application persisted successfully.");
    }

    private void persistVersion(final Version version)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        this.log.write(this, "Version is new, persisting version...");

        final ApiRequest<Application> request = RequestFactory.createPersistVersionRequest(this.store, version);
        final ApiResponse<Application> response = this.requestManager.execute(request, this.log);

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting version.");
            Builds.set(this.build, Result.UNSTABLE, this.log);
            return;
        }

        this.log.write(this, "Version persisted successfully.");
    }

    private List<ApiResponse<Asset>> uploadAssets(final File basePath, final String includes, final String excludes)
            throws URISyntaxException, InterruptedException {

        if (StringUtils.isBlank(includes)) {
            this.log.write(this, "No file to upload specified, filter expression is empty, upload failed.");
            return null;
        }

        if (!StringUtils.isBlank(excludes)) {
            this.log.write(this, "Excluding files that match \"%s\"", excludes);
        }

        final FileSet fileSet = Util.createFileSet(basePath, includes, excludes);
        final File directory = fileSet.getDirectoryScanner().getBasedir();

        if (fileSet.getDirectoryScanner().getIncludedFilesCount() < 1) {
            this.log.write(this, "The file specified by \"%s\" does not exist, upload failed.", includes);
            return null;
        }

        final List<ApiResponse<Asset>> responses = new ArrayList<ApiResponse<Asset>>();

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            final ApiResponse<Asset> response = this.uploadAsset(directory, fileName);

            if (response != null) {
                responses.add(response);
            }
        }

        return responses;
    }

    private ApiResponse<Asset> uploadAsset(final File directory, final String fileName)
            throws URISyntaxException, InterruptedException {

        try {
            final Stopwatch sw = new Stopwatch();
            final File file = new File(directory, fileName);
            final ApiRequest<Asset> request = RequestFactory.createUploadRequest(this.store, file);

            this.log.write(this, "Uploading \"%s\" (%,d Byte)...", fileName, file.length());

            sw.start();
            final ApiResponse<Asset> response = this.requestManager.execute(request, this.log);
            sw.stop();

            final String speed = this.getUploadSpeed(sw, file);
            this.log.write(this, "Upload of file completed (%s, %s).", sw, speed);

            return response;

        } catch (final IOException e) {
            this.log.write(this, "Upload of file failed, error during execution:\n\n%s\n", e);
            Builds.set(this.build, Result.UNSTABLE, this.log);

        } catch (final ExecutionException e) {
            this.log.write(this, "Upload of file failed, error during execution:\n\n%s\n", e);
            Builds.set(this.build, Result.UNSTABLE, this.log);

        }
        return null;
    }

    private String getUploadSpeed(final Stopwatch sw, final File file) {

        final float seconds = sw.elapsedTime(TimeUnit.SECONDS);

        if (file.length() == 0 || seconds == 0) {
            return "Unknown";
        }

        final String[] units = {"", "K", "M", "G"};

        float speed = file.length() / seconds;
        int index = 0;

        while (speed > 2048 && index < units.length) {
            speed /= 1024;
            ++index;
        }

        return String.format("%,.0f %sB/s", speed, units[index]);
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
            this.log.write(this, "The file specified by \"%s\" does not exist.", filePath);
        }

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            this.log.write(this, "Reading file \"%s\"...", fileName);
            final File file = new File(directory, fileName);
            this.readFile(file, sb);
        }
        return this.getEllipsizedText(sb.toString(), MAX_TEXT_LENGTH);
    }

    private void readFile(final File file, final StringBuilder sb) {

        try {
            final BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null && sb.length() < MAX_TEXT_LENGTH) {
                sb.append(line);
                sb.append("\n");
            }

            if (sb.length() >= MAX_TEXT_LENGTH) {
                this.log.write(this, "Text in file \"%s\" exceeds %d characters and will be truncated.", file.getName(), MAX_TEXT_LENGTH);
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
            this.log.write(
                    this,
                    "Error uploading file (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

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
            this.log.write(
                    this,
                    "Error creating application object (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

            return false;
        }

        if (this.isEmpty(applications)) {
            this.log.write(this, "Error creating application object, the server returned no application objects.");
            return false;
        }

        return true;
    }

    private boolean verifyDeleteResponse(final ApiResponse<String> response) {

        this.log.write(this, "Status: %d", response.getStatus());

        if (response.getStatus() != 0) {
            this.log.write(
                    this,
                    "Error deleting application object (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

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

    @Override
    public void checkRoles(final RoleChecker roleChecker) throws SecurityException {
    }
}
