
package org.jenkinsci.plugins.relution_publisher.builder;

import static org.assertj.core.api.Assertions.assertThat;

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.model.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.model.Artifact;
import org.jenkinsci.plugins.relution_publisher.model.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.model.UploadMode;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.unittest.mocks.MockLog;
import org.jenkinsci.plugins.relution_publisher.unittest.mocks.MockNetwork;
import org.jenkinsci.plugins.relution_publisher.unittest.mocks.ResponseBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import hudson.model.Result;


public class SingleRequestUploaderTest {

    private final RequestFactory  requestFactory  = new RequestFactory();
    private final MockNetwork     network         = new MockNetwork();
    private final Log             log             = new MockLog();

    private final Store           store           = new Store(
            "store-id",
            "https://store.example.com",
            "organization",
            "username",
            "password",
            ReleaseStatus.DEVELOPMENT.key,
            ArchiveMode.ARCHIVE.key,
            UploadMode.SUCCESS.key,
            "proxyHost",
            8080,
            "proxyUsername",
            "proxyPassword");

    private final Publication     publication     = new Publication(
            "**/build/outputs/apk/example-*.apk",
            null,
            "store-id",
            ReleaseStatus.DEFAULT.key,
            ArchiveMode.DEFAULT.key,
            UploadMode.DEFAULT.key,
            "name",
            "iconPath",
            "changelog.txt",
            "description.txt",
            "versionName",
            "environment-uuid");

    private final ResponseBuilder responseBuilder = new ResponseBuilder();

    @Before
    public void init() throws IOException {
        final File root = new File("./project/build/outputs/apk");
        root.mkdirs();

        final File file = new File(root, "example-1.apk");
        file.createNewFile();
    }

    @Test
    public void shouldBeSuccessOnCreateResponse() throws IOException, ExecutionException, InterruptedException {
        final Uploader uploader = new SingleRequestUploader(this.requestFactory, this.network, this.log);
        final Artifact artifact = new Artifact(this.store, new File("."), this.publication, Result.SUCCESS);
        final ApiResponse response = this.responseBuilder.create("post-apps-201.json", 201, "Success");
        this.network.add(response);

        final Result result = uploader.publish(artifact);

        assertThat(result).isEqualTo(Result.SUCCESS);
    }

    @Test
    public void shouldBeUnstableOnAlreadyExistsResponse() throws IOException, ExecutionException, InterruptedException {
        final Uploader uploader = new SingleRequestUploader(this.requestFactory, this.network, this.log);
        final Artifact artifact = new Artifact(this.store, new File("."), this.publication, Result.SUCCESS);
        final ApiResponse response = this.responseBuilder.create("post-apps-422.json", 422, "Success");
        this.network.add(response);

        final Result result = uploader.publish(artifact);

        assertThat(result).isEqualTo(Result.UNSTABLE);
    }

    @Test
    public void shouldBeUnstableOnEmptyResponse() throws IOException, ExecutionException, InterruptedException {
        final Uploader uploader = new SingleRequestUploader(this.requestFactory, this.network, this.log);
        final Artifact artifact = new Artifact(this.store, new File("."), this.publication, Result.SUCCESS);

        final Result result = uploader.publish(artifact);

        assertThat(result).isEqualTo(Result.UNSTABLE);
    }

    @Test
    public void shouldBeNotBuildIfFileMissing() throws IOException, ExecutionException, InterruptedException {
        final Uploader uploader = new SingleRequestUploader(this.requestFactory, this.network, this.log);
        final Artifact artifact = new Artifact(this.store, new File("."), this.publication, Result.SUCCESS);
        this.publication.setArtifactPath("**/build/outputs/apk/missing-*.apk");

        final Result result = uploader.publish(artifact);

        assertThat(result).isEqualTo(Result.NOT_BUILT);
    }
}
