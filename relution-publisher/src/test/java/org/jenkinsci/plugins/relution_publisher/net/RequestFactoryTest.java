
package org.jenkinsci.plugins.relution_publisher.net;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.constants.UploadMode;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest.Method;
import org.jenkinsci.plugins.relution_publisher.net.requests.EntityRequest;
import org.junit.Before;
import org.junit.Test;


public class RequestFactoryTest {

    private static final String URL_HOST_NAME       = "https://example.com";
    private static final String URL_HOST_NAME_SLASH = "https://example.com/";

    private static final String URL_API_URL         = "https://example.com/relution/api/v1";
    private static final String URL_API_URL_SLASH   = "https://example.com/relution/api/v1/";

    private final Store         store               = new Store(
            "store-1-uuid",
            "https://example.com",
            "test",
            "test",
            "test",
            ReleaseStatus.DEFAULT.key,
            ArchiveMode.DEFAULT.key,
            UploadMode.DEFAULT.key,
            null,
            0,
            null,
            null);

    private final JsonObject    app                 = new JsonObject();
    private final JsonObject    version             = new JsonObject();

    @Before
    public void initialize() {
        this.app.addProperty("uuid", "{app-uuid}");

        this.version.addProperty("uuid", "{version-uuid}");
        this.version.addProperty("appUuid", "{app-uuid}");
    }

    @Test
    public void shouldCreateLoginRequestFromHostName() {
        this.store.setUrl(URL_HOST_NAME);

        final EntityRequest request = RequestFactory.createLoginRequest(this.store);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/gofer/security/rest/auth/login");
    }

    @Test
    public void shouldCreateLoginRequestFromApiUrl() {
        this.store.setUrl(URL_API_URL);

        final EntityRequest request = RequestFactory.createLoginRequest(this.store);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/gofer/security/rest/auth/login");
    }

    @Test
    public void shouldCreateLoginRequestFromHostNameSlash() {
        this.store.setUrl(URL_HOST_NAME_SLASH);

        final EntityRequest request = RequestFactory.createLoginRequest(this.store);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/gofer/security/rest/auth/login");
    }

    @Test
    public void shouldCreateLoginRequestFromApiUrlSlash() {
        this.store.setUrl(URL_API_URL_SLASH);

        final EntityRequest request = RequestFactory.createLoginRequest(this.store);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/gofer/security/rest/auth/login");
    }

    @Test
    public void shouldCreateLogoutUrlFromHostName() {
        this.store.setUrl(URL_HOST_NAME);

        final EntityRequest request = RequestFactory.createLogoutRequest(this.store);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/gofer/security/rest/auth/logout");
    }

    @Test
    public void shouldCreateLogoutUrlFromApiUrl() {
        this.store.setUrl(URL_API_URL);

        final EntityRequest request = RequestFactory.createLogoutRequest(this.store);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/gofer/security/rest/auth/logout");
    }

    @Test
    public void shouldCreatePersistAppRequestFromHostName() {
        this.store.setUrl(URL_HOST_NAME);
        final JsonObject app = new JsonObject();
        app.addProperty("appUuid", "{app-uuid}");

        final EntityRequest request = RequestFactory.createPersistApplicationRequest(this.store, app);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/relution/api/v1/apps");
    }

    @Test
    public void shouldCreatePersistAppRequestFromApiUrl() {
        this.store.setUrl(URL_API_URL);
        final JsonObject app = new JsonObject();
        app.addProperty("uuid", "{app-uuid}");

        final EntityRequest request = RequestFactory.createPersistApplicationRequest(this.store, app);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/relution/api/v1/apps");
    }

    @Test
    public void shouldCreatePersistVersionRequestFromHostName() {
        this.store.setUrl(URL_HOST_NAME);

        final EntityRequest request = RequestFactory.createPersistVersionRequest(this.store, this.app, this.version);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/relution/api/v1/apps/{app-uuid}/versions");
    }

    @Test
    public void shouldCreatePersistVersionRequestFromApiUrl() {
        this.store.setUrl(URL_API_URL);

        final EntityRequest request = RequestFactory.createPersistVersionRequest(this.store, this.app, this.version);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.POST);
        assertThat(request.getUri()).isEqualTo("https://example.com/relution/api/v1/apps/{app-uuid}/versions");
    }

    @Test
    public void shouldCreateDeleteVersionRequestFromHostName() {
        this.store.setUrl(URL_HOST_NAME);

        final EntityRequest request = RequestFactory.createDeleteVersionRequest(this.store, this.version);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.DELETE);
        assertThat(request.getUri()).isEqualTo("https://example.com/relution/api/v1/apps/{app-uuid}/versions/{version-uuid}");
    }

    @Test
    public void shouldCreateDeleteVersionRequestFromApiUrl() {
        this.store.setUrl(URL_API_URL);

        final EntityRequest request = RequestFactory.createDeleteVersionRequest(this.store, this.version);

        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo(Method.DELETE);
        assertThat(request.getUri()).isEqualTo("https://example.com/relution/api/v1/apps/{app-uuid}/versions/{version-uuid}");
    }
}
