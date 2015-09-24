/*
 * Copyright (c) 2013-2015 M-Way Solutions GmbH
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;
import org.jenkinsci.plugins.relution_publisher.constants.ApiObject;
import org.jenkinsci.plugins.relution_publisher.constants.App;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.constants.Language;
import org.jenkinsci.plugins.relution_publisher.constants.Version;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.RequestManager;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.util.Builds;
import org.jenkinsci.plugins.relution_publisher.util.Json;
import org.jenkinsci.remoting.RoleChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import hudson.FilePath.FileCallable;
import hudson.Util;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;


/**
 * Uploads build artifacts to a {@link Store} that has been specified in a Jenkins project's
 * post-build action, in the form of a {@link Publication}.
 */
public class ArtifactFileUploader implements FileCallable<Boolean> {

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

    /**
     * Maximum length of text to upload.
     */
    private static final int MAX_TEXT_LENGTH = 49152;

    private Result result;

    private final Publication publication;
    private final Store       store;
    private final Log         log;

    private Set<String> locales;

    private final RequestManager requestManager;

    /**
     * Initializes a new instance of the {@link ArtifactFileUploader} class.
     * @param result The build that produced the artifact to be published.
     * @param publication The {@link Publication} that describes the artifact to be published.
     * @param store The {@link Store} to which the publication should be published.
     * @param log The {@link Log} to write log messages to.
     */
    public ArtifactFileUploader(final Result result, final Publication publication, final Store store, final Log log) {

        this.result = result;

        this.publication = publication;
        this.store = store;
        this.log = log;

        this.requestManager = new RequestManager();
        this.requestManager.setProxy(store.getProxyHost(), store.getProxyPort());
        this.requestManager.setProxyCredentials(store.getProxyUsername(), store.getProxyPassword());
    }

    @Override
    public Boolean invoke(final File basePath, final VirtualChannel channel)
            throws IOException, InterruptedException {

        try {
            this.log.write(this, "Uploading build artifacts…");
            final List<JsonObject> assets = this.uploadAssets(
                    basePath,
                    this.publication.getArtifactPath(),
                    this.publication.getArtifactExcludePath());

            if (this.isEmpty(assets) && this.result == Result.UNSTABLE) {
                this.log.write(this, "Upload of build artifacts failed.");
                return true;

            } else if (this.isEmpty(assets)) {
                this.log.write(this, "No artifacts to upload found.");
                Builds.setResult(this, Result.NOT_BUILT, this.log);
                return true;
            }

            for (final JsonObject asset : assets) {
                this.log.write();
                this.retrieveApplication(basePath, asset);
            }

        } catch (final IOException e) {
            this.log.write(this, "Publication failed.\n\n%s\n", e);
            Builds.setResult(this, Result.UNSTABLE, this.log);

        } catch (final URISyntaxException e) {
            this.log.write(this, "Publication failed.\n\n%s\n", e);
            Builds.setResult(this, Result.UNSTABLE, this.log);

        } catch (final ExecutionException e) {
            this.log.write(this, "Publication failed.\n\n%s\n", e);
            Builds.setResult(this, Result.UNSTABLE, this.log);

        } finally {
            this.requestManager.shutdown();

        }

        return true;
    }

    private void retrieveApplication(final File basePath, final JsonObject asset)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        this.log.write(this, "Requesting app associated with asset {%s}…", Json.getString(asset, ApiObject.UUID));
        final ApiRequest request = RequestFactory.createAppFromFileRequest(this.store, asset);
        final ApiResponse response = this.requestManager.execute(request, this.log);

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Retrieval of app failed.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return;
        }

        final JsonArray applications = response.getResults();
        final JsonObject app = this.getApplication(applications, asset);

        if (Json.isNull(app)) {
            Builds.setResult(this, Result.UNSTABLE, this.log);
            this.log.write(this, "Could not find app associated with uploaded file.");
            return;
        }

        this.log.write(this, "App \"%s\" was retrieved.", Json.getString(app, App.INTERNAL_NAME));
        this.log.write(this, "Searching app version associated with uploaded file…");
        final JsonObject version = this.getVersion(app, asset);

        if (Json.isNull(version)) {
            this.log.write(this, "Could not find app version associated with uploaded file.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return;
        }

        this.log.write(this, "Found app version \"%s\".", Json.getString(version, Version.VERSION_NAME));
        this.setVersionMetadata(basePath, version);

        if (Json.isNull(app, ApiObject.UUID)) {
            this.persistApplication(app);

        } else {
            if (this.persistVersion(app, version)) {
                this.manageArchivedVersions(app, version);
            }
        }

        this.log.write(
                this,
                "Uploaded app version \"%s\" (%d) to \"%s\"",
                Json.getString(version, Version.VERSION_NAME),
                Json.getInt(version, Version.VERSION_CODE),
                Json.getString(version, Version.RELEASE_STATUS));
    }

    private List<JsonObject> getArchivedVersions(final JsonObject app, final JsonObject newVersion) {
        final String newReleaseStatus = Json.getString(newVersion, Version.RELEASE_STATUS);
        final int newVersionCode = Json.getInt(newVersion, Version.VERSION_CODE);

        final List<JsonObject> archived = new ArrayList<JsonObject>();
        final JsonArray versions = Json.getArray(app, App.VERSIONS);

        for (final JsonElement element : versions) {
            final JsonObject oldVersion = element.getAsJsonObject();

            final String oldReleaseStatus = Json.getString(oldVersion, Version.RELEASE_STATUS);
            final int oldVersionCode = Json.getInt(oldVersion, Version.VERSION_CODE);

            if (StringUtils.equals(oldReleaseStatus, newReleaseStatus) && oldVersionCode != newVersionCode) {
                archived.add(oldVersion);
            }
        }

        return archived;
    }

    private void manageArchivedVersions(final JsonObject app, final JsonObject version)
            throws URISyntaxException, InterruptedException, ExecutionException {

        final String archiveMode = !this.publication.usesDefaultArchiveMode()
                ? this.publication.getArchiveMode()
                : this.store.getArchiveMode();

        if (StringUtils.equals(archiveMode, ArchiveMode.OVERWRITE.key)) {
            this.log.write(this, "Delete previous app version from \"%s\"", Json.getString(version, Version.RELEASE_STATUS));
            final List<JsonObject> archived = this.getArchivedVersions(app, version);

            for (final JsonObject current : archived) {
                this.deleteVersion(current);
            }

        } else {
            this.log.write(this, "Keep previous app version (moved to archive)");

        }
    }

    private void deleteVersion(final JsonObject version) throws URISyntaxException, InterruptedException, ExecutionException {
        this.log.write(
                this,
                "Deleting app version \"%s\" (%d) from \"%s\"…",
                Json.getString(version, Version.VERSION_NAME),
                Json.getInt(version, Version.VERSION_CODE),
                Json.getString(version, Version.RELEASE_STATUS));

        try {
            final ApiRequest request = RequestFactory.createDeleteVersionRequest(this.store, version);
            final ApiResponse response = this.requestManager.execute(request, this.log);

            if (!this.verifyDeleteResponse(response)) {
                this.log.write(this, "Error deleting app version");
                Builds.setResult(this, Result.UNSTABLE, this.log);
                return;
            }

        } catch (final IOException e) {
            this.log.write(this, "Error deleting app version: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setVersionMetadata(final File basePath, final JsonObject version)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        this.setReleaseStatus(version);

        this.setName(version);
        this.setIcon(basePath, version);

        this.setChangeLog(basePath, version);
        this.setDescription(basePath, version);

        this.setVersionName(version);
    }

    private void setReleaseStatus(final JsonObject version) {
        final String releaseStatus = !this.publication.usesDefaultReleaseStatus()
                ? this.publication.getReleaseStatus()
                : this.store.getReleaseStatus();

        if (!StringUtils.isBlank(releaseStatus)) {
            version.addProperty("releaseStatus", releaseStatus);
        }
    }

    private void setName(final JsonObject version) throws IOException, InterruptedException, ExecutionException {
        if (StringUtils.isBlank(this.publication.getName())) {
            this.log.write(this, "No name set, default name will be used.");
            return;
        }

        this.setText("name", version.get(Version.NAME), this.publication.getName());
    }

    private void setIcon(final File basePath, final JsonObject version)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {

        if (StringUtils.isBlank(this.publication.getIconPath())) {
            this.log.write(this, "No icon set, default icon will be used.");
            return;
        }

        this.log.write(this, "Uploading app icon…");
        final String filePath = this.publication.getIconPath();
        final List<JsonObject> assets = this.uploadAssets(basePath, filePath, null);

        if (assets.size() != 1) {
            this.log.write(this, "More than one unpersisted asset returned by server.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return;
        }

        version.add("icon", assets.get(0));
    }

    private void setChangeLog(final File basePath, final JsonObject version)
            throws IOException, InterruptedException, ExecutionException {

        if (StringUtils.isBlank(this.publication.getChangeLogPath())) {
            this.log.write(this, "The change log path is empty, nothing to set.");
            return;
        }

        final String filePath = this.publication.getChangeLogPath();
        final String changeLogText = this.readFile(basePath, filePath);
        this.setText("change log", version.get(Version.CHANGE_LOG), changeLogText);
    }

    private void setDescription(final File basePath, final JsonObject version)
            throws IOException, InterruptedException, ExecutionException {

        if (StringUtils.isBlank(this.publication.getDescriptionPath())) {
            this.log.write(this, "The description path is empty, nothing to set.");
            return;
        }

        final String filePath = this.publication.getDescriptionPath();
        final String descriptionText = this.readFile(basePath, filePath);
        this.setText("description", version.get(Version.DESCRIPTION), descriptionText);
    }

    private void setText(final String item, final JsonElement element, final String text) throws IOException, InterruptedException, ExecutionException {
        if (StringUtils.isBlank(text)) {
            this.log.write(this, "The %s is empty, nothing to set.", item);
            return;
        }

        final String ellipsized = this.getEllipsizedText(text.replace("\n", "<br/>"), 50);
        this.log.write(this, "Set %s to: \"%s\" (%d characters)", item, ellipsized, text.length());

        // Add the text for each locale
        final JsonObject localizedString = element.getAsJsonObject();
        final Set<String> locales = this.getLocales();

        for (final String locale : locales) {
            this.log.write(this, "Set %s for locale %s.", item, locale);
            localizedString.addProperty(locale, text);
        }
    }

    private Set<String> getLocales() throws IOException, InterruptedException, ExecutionException {
        if (this.locales != null) {
            return this.locales;
        }

        this.log.write(this, "Requesting configured languages…");

        final ApiRequest request = RequestFactory.createLanguageRequest(this.store);
        final ApiResponse response = this.requestManager.execute(request, this.log);

        final JsonArray languages = response.getResults();
        this.locales = new HashSet<String>(languages.size());

        for (final JsonElement element : languages) {
            final JsonObject language = element.getAsJsonObject();
            final String name = Json.getString(language, Language.NAME);
            final String locale = Json.getString(language, Language.LOCALE);

            this.log.write(this, "%s: %s", name, locale);
            this.locales.add(locale);
        }

        return this.locales;
    }

    private void setVersionName(final JsonObject version) {
        if (StringUtils.isBlank(this.publication.getVersionName())) {
            this.log.write(this, "No version name set, default name will be used.");
            return;
        }
        version.addProperty("versionName", this.publication.getVersionName());
    }

    private boolean persistApplication(final JsonObject app) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        this.log.write(this, "App is new, persisting app…");

        final ApiRequest request = RequestFactory.createPersistApplicationRequest(this.store, app);
        final ApiResponse response = this.requestManager.execute(request, this.log);

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting app.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return false;
        }

        this.log.write(this, "App persisted successfully.");
        return true;
    }

    private boolean persistVersion(final JsonObject app, final JsonObject version)
            throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        this.log.write(this, "App version is new, persisting app version…");

        final ApiRequest request = RequestFactory.createPersistVersionRequest(this.store, app, version);
        final ApiResponse response = this.requestManager.execute(request, this.log);

        if (!this.verifyApplicationResponse(response)) {
            this.log.write(this, "Error persisting app version.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return false;
        }

        this.log.write(this, "App version persisted successfully.");
        return true;
    }

    private List<JsonObject> uploadAssets(final File basePath, final String includes, final String excludes)
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

        final List<JsonObject> assets = new ArrayList<JsonObject>();

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            final JsonObject asset = this.uploadAsset(directory, fileName);

            if (asset != null) {
                assets.add(asset);
            }
        }

        return assets;
    }

    private JsonObject uploadAsset(final File directory, final String fileName)
            throws URISyntaxException, InterruptedException {

        try {
            final Stopwatch sw = new Stopwatch();
            final File file = new File(directory, fileName);
            final ApiRequest request = RequestFactory.createUploadRequest(this.store, file);

            this.log.write(this, "Uploading \"%s\" (%,d Byte)…", fileName, file.length());

            sw.start();
            final ApiResponse response = this.requestManager.execute(request, this.log);
            sw.stop();

            final String speed = this.getUploadSpeed(sw, file);
            this.log.write(this, "Upload of file completed (%s, %s).", sw, speed);

            return this.extractAsset(response);

        } catch (final IOException e) {
            this.log.write(this, "Upload of file failed, error during execution:\n\n%s\n", e);
            Builds.setResult(this, Result.UNSTABLE, this.log);

        } catch (final ExecutionException e) {
            this.log.write(this, "Upload of file failed, error during execution:\n\n%s\n", e);
            Builds.setResult(this, Result.UNSTABLE, this.log);

        }
        return null;
    }

    private JsonObject extractAsset(final ApiResponse response) {
        if (response == null) {
            this.log.write(this, "Error during upload, server's response is empty.");
            return null;
        }

        if (!this.verifyAssetResponse(response)) {
            this.log.write(this, "Upload of asset failed.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return null;
        }

        final JsonArray assets = response.getResults();

        if (assets.size() != 1) {
            this.log.write(this, "Error during upload, more than one asset returned by server.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return null;
        }

        final JsonObject asset = Json.getObject(assets, 0);

        if (Json.isNull(asset)) {
            this.log.write(this, "Error during upload, asset is null.");
            Builds.setResult(this, Result.UNSTABLE, this.log);
            return null;
        }

        this.log.write(this, "Upload completed, received asset {%s}", Json.getString(asset, ApiObject.UUID));
        return asset;
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

    private String readFile(final File basePath, final String filePath) {

        final FileSet fileSet = Util.createFileSet(basePath, filePath);
        final File directory = fileSet.getDirectoryScanner().getBasedir();
        final StringBuilder sb = new StringBuilder();

        if (fileSet.getDirectoryScanner().getIncludedFilesCount() < 1) {
            this.log.write(this, "The file specified by \"%s\" does not exist.", filePath);
        }

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            this.log.write(this, "Reading file \"%s\"…", fileName);
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

    private JsonObject getApplication(final JsonArray applications, final JsonObject asset) {
        for (final JsonElement element : applications) {
            final JsonObject app = element.getAsJsonObject();
            final JsonObject version = this.getVersion(app, asset);

            if (version != null) {
                return app;
            }
        }
        return null;
    }

    private JsonObject getVersion(final JsonObject app, final JsonObject asset) {
        final String uuid = Json.getString(asset, ApiObject.UUID);
        final JsonArray versions = Json.getArray(app, App.VERSIONS);

        for (final JsonElement element : versions) {
            final JsonObject version = element.getAsJsonObject();
            final JsonObject file = Json.getObject(version, Version.FILE);

            if (file != null) {
                final String fileUuid = Json.getString(file, ApiObject.UUID);

                if (StringUtils.equals(fileUuid, uuid)) {
                    return version;
                }
            }
        }
        return null;
    }

    private boolean verifyAssetResponse(final ApiResponse response) {
        final JsonArray assets = response.getResults();

        if (response.getStatus() != 0) {
            this.log.write(
                    this,
                    "Error uploading file (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

            return false;
        }

        if (Json.isEmpty(assets)) {
            this.log.write(this, "Error uploading file, the server returned no assets.");
            return false;
        }

        return true;
    }

    private boolean verifyApplicationResponse(final ApiResponse response) {
        final JsonArray applications = response.getResults();

        if (response.getStatus() != 0) {
            this.log.write(
                    this,
                    "Error creating app (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

            return false;
        }

        if (Json.isEmpty(applications)) {
            this.log.write(this, "Error creating app, the server returned no apps.");
            return false;
        }

        return true;
    }

    private boolean verifyDeleteResponse(final ApiResponse response) {
        this.log.write(this, "Status: %d", response.getStatus());

        if (response.getStatus() != 0) {
            this.log.write(
                    this,
                    "Error deleting app version (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

            return false;
        }

        return true;
    }

    private String getEllipsizedText(final String input, final int maxLen) {
        if (StringUtils.isEmpty(input) || input.length() <= maxLen) {
            return input;
        }
        return input.substring(0, maxLen - 1) + "…";
    }

    private boolean isEmpty(final Collection<?> collection) {
        return (collection == null || collection.size() == 0);
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    @Override
    public void checkRoles(final RoleChecker roleChecker) throws SecurityException {
    }
}
