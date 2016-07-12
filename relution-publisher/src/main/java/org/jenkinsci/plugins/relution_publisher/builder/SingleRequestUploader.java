/*
 * Copyright (c) 2016 M-Way Solutions GmbH
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

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.model.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.model.Artifact;
import org.jenkinsci.plugins.relution_publisher.net.Network;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.requests.ZeroCopyFileRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.util.Builds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import hudson.Util;
import hudson.model.Result;


public class SingleRequestUploader implements Uploader {

    private final RequestFactory requestFactory;
    private final Network        network;
    private final Log            log;

    public SingleRequestUploader(
            final RequestFactory requestFactory,
            final Network network,
            final Log log) {
        this.requestFactory = requestFactory;
        this.network = network;
        this.log = log;
    }

    @Override
    public Result publish(final Artifact artifact)
            throws InterruptedException, IOException, ExecutionException {
        final FileSet fileSet = this.getArtifactFiles(artifact);
        this.publish(artifact, fileSet);
        return artifact.getResult();
    }

    private void publish(final Artifact artifact, final FileSet fileSet) throws InterruptedException {
        if (fileSet == null) {
            this.log.write(this, "No build artifacts found, upload failed.");
            Builds.setResult(artifact, Result.NOT_BUILT, this.log);
            return;
        }

        final File changelog = this.getChangeLog(artifact);
        if (changelog == null) {
            this.log.write(this, "No change log set");
        }

        for (final String fileName : fileSet.getDirectoryScanner().getIncludedFiles()) {
            try {
                this.publish(artifact, fileSet, fileName, changelog);

            } catch (final IOException e) {
                this.log.write(this, "Upload of file failed, error during execution:\n\n%s\n", e);
                Builds.setResult(artifact, Result.UNSTABLE, this.log);

            } catch (final ExecutionException e) {
                this.log.write(this, "Upload of file failed, error during execution:\n\n%s\n", e);
                Builds.setResult(artifact, Result.UNSTABLE, this.log);

            }
        }
    }

    private void publish(final Artifact artifact, final FileSet fileSet, final String fileName, final File changelog)
            throws IOException, InterruptedException, ExecutionException {
        this.log.write();
        this.log.write(this, "Uploading %s…", fileName);

        final File app = new File(fileSet.getDirectoryScanner().getBasedir(), fileName);
        final ApiResponse upload = this.upload(artifact, app, changelog);

        if (!this.verifyUpload(upload)) {
            Builds.setResult(artifact, Result.UNSTABLE, this.log);
        }
    }

    private ApiResponse upload(final Artifact artifact, final File app, final File changelog)
            throws IOException, InterruptedException, ExecutionException {
        final Store store = artifact.getStore();
        final String releaseStatus = this.getReleaseStatus(artifact);
        final boolean archivePreviousVersion = this.isArchivePreviousVersion(artifact);
        final String environmentUuid = this.getEnvironmentUuid(artifact);

        this.log.write(this, "- Release status          : %s", releaseStatus);
        this.log.write(this, "- Archive previous version: %s", archivePreviousVersion);
        this.log.write(this, "- Environment             : %s", environmentUuid);

        final ZeroCopyFileRequest request = this.requestFactory.createUploadAppRequest(store, releaseStatus, archivePreviousVersion, environmentUuid);
        this.log.write(this, "- App                     : %,d Byte", app.length());
        request.addItem("app", app);

        if (changelog != null) {
            this.log.write(this, "- Change log              : %,d Byte", changelog.length());
            request.addItem("changelog", changelog);
        }

        final Stopwatch sw = new Stopwatch();

        sw.start();
        final ApiResponse response = this.network.execute(request, this.log);
        sw.stop();

        final String speed = this.getUploadSpeed(sw, request);
        this.log.write(this, "Upload completed (%s, %s)", sw, speed);

        return response;
    }

    private boolean verifyUpload(final ApiResponse response) {
        if (response == null) {
            this.log.write(this, "Error during upload, server's response is empty");
            return false;
        }

        if (response.getStatus() != 0) {
            this.log.write(
                    this,
                    "Error uploading file (%d), server's response:\n\n%s\n",
                    response.getStatusCode(),
                    response.getMessage());

            return false;
        }

        this.log.write(this, "Upload completed with success (%d)", response.getStatusCode());
        return true;
    }

    private FileSet getArtifactFiles(final Artifact artifact) {
        final Publication publication = artifact.getPublication();
        final String includes = publication.getArtifactPath();
        final String excludes = publication.getArtifactExcludePath();

        this.log.write();
        this.log.write(this, "Find artifact files to upload…");
        return this.getFileSet(artifact, includes, excludes);
    }

    private File getChangeLog(final Artifact artifact) {
        final Publication publication = artifact.getPublication();
        final String path = publication.getChangeLogPath();

        this.log.write(this, "Find change log…");
        return this.getFile(artifact, path, null);
    }

    private String getReleaseStatus(final Artifact artifact) {
        return !artifact.getPublication().usesDefaultReleaseStatus()
                ? artifact.getPublication().getReleaseStatus()
                : artifact.getStore().getReleaseStatus();
    }

    private boolean isArchivePreviousVersion(final Artifact artifact) {
        final String archiveMode = !artifact.getPublication().usesDefaultArchiveMode()
                ? artifact.getPublication().getArchiveMode()
                : artifact.getStore().getArchiveMode();

        return !(StringUtils.equals(archiveMode, ArchiveMode.OVERWRITE.key));
    }

    private String getEnvironmentUuid(final Artifact artifact) {
        final Publication publication = artifact.getPublication();
        return publication.getEnvironmentUuid();
    }

    private FileSet getFileSet(final Artifact artifact, final String includes, final String excludes) {
        if (StringUtils.isBlank(includes)) {
            this.log.write(this, "Filter expression is empty, no files to include");
            return null;
        }

        this.log.write(this, "Including files that match \"%s\"", includes);

        if (!StringUtils.isBlank(excludes)) {
            this.log.write(this, "Excluding files that match \"%s\"", excludes);
        }

        final FileSet fileSet = Util.createFileSet(artifact.getBasePath(), includes, excludes);
        final DirectoryScanner scanner = fileSet.getDirectoryScanner();
        final int includedFilesCount = scanner.getIncludedFilesCount();

        if (includedFilesCount < 1) {
            this.log.write(this, "No file(s) found that match \"%s\"", includes);
            return null;
        } else if (includedFilesCount == 1) {
            final String fileName = scanner.getIncludedFiles()[0];
            this.log.write(this, "Found \"%s\"", fileName);
        } else {
            this.log.write(this, "Found %d files", includedFilesCount);
        }

        return fileSet;
    }

    private File getFile(final Artifact artifact, final String includes, final String excludes) {
        final FileSet fileSet = this.getFileSet(artifact, includes, excludes);

        if (fileSet == null) {
            return null;
        }

        final DirectoryScanner scanner = fileSet.getDirectoryScanner();

        if (scanner.getIncludedFilesCount() > 1) {
            return null;
        }

        final String fileName = scanner.getIncludedFiles()[0];
        return new File(scanner.getBasedir(), fileName);
    }

    private String getUploadSpeed(final Stopwatch sw, final ZeroCopyFileRequest request) throws FileNotFoundException {
        final float milliseconds = sw.elapsedTime(TimeUnit.MILLISECONDS);
        final float seconds = milliseconds / 1000f;

        final long length = request.getContentLength();

        if (length == 0 || seconds == 0) {
            return "Unknown";
        }

        final String[] units = {"", "K", "M", "G"};

        float speed = length / seconds;
        int index = 0;

        while (speed > 2048 && index < units.length) {
            speed /= 1024;
            ++index;
        }

        return String.format("%,.0f %sB/s", speed, units[index]);
    }
}
