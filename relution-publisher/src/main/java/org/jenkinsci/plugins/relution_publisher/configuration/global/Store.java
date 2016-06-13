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

package org.jenkinsci.plugins.relution_publisher.configuration.global;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.constants.UploadMode;
import org.jenkinsci.plugins.relution_publisher.model.ServerVersion;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.SessionManager;
import org.jenkinsci.plugins.relution_publisher.net.requests.BaseRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.util.ErrorType;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;


/**
 * Represents a Relution Enterprise App Store that is used as a communication endpoint by the
 * plugin. A store is uniquely identified by its URL and the credentials required
 * to connect to the store.
 * <p>
 * Additionally it is possible to define the default {@link ReleaseStatus} to use when uploading
 * a version to this store. The default release status can be overridden on a per Jenkins project
 * basis.
 */
public class Store extends AbstractDescribableImpl<Store> implements Serializable {

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
    private static final long     serialVersionUID   = 1L;

    public final static String    KEY_ID             = "id";
    public final static String    KEY_URL            = "url";

    public final static String    KEY_USERNAME       = "username";
    public final static String    KEY_PASSWORD       = "password";

    public final static String    KEY_RELEASE_STATUS = "releaseStatus";
    public final static String    KEY_ARCHIVE_MODE   = "archiveMode";
    public final static String    KEY_UPLOAD_MODE    = "uploadMode";

    public final static String    KEY_PROXY_HOST     = "proxyHost";
    public final static String    KEY_PROXY_PORT     = "proxyPort";

    public final static String    KEY_PROXY_USERNAME = "proxyUsername";
    public final static String    KEY_PROXY_PASSWORD = "proxyPassword";

    private final static String[] URL_SCHEMES        = {"http", "https"};

    private String                mId;
    private String                mUrl;

    private String                mUsername;
    private String                mPassword;

    private String                mReleaseStatus;
    private String                mArchiveMode;
    private String                mUploadMode;

    private String                mProxyHost;
    private int                   mProxyPort;

    private String                mProxyUsername;
    private String                mProxyPassword;

    /**
     * Creates a new instance of the {@link Store} class initialized with the values in
     * the specified JSON string.
     * @param storeJsonString A JSON formatted string.
     * @return A new instance of the {@link Store} class.
     * @exception JSONException The specified string could not be converted to a {@link JSONObject}.
     */
    public static Store fromJson(final String storeJsonString) {
        final JSONObject storeJsonObject = JSONObject.fromObject(storeJsonString);
        return new Store(storeJsonObject);
    }

    /**
     * Initializes a new instance of the {@link Store} class.
     * @param id The unique identifier of the store.
     * @param url The URL of the store.
     * @param organization The organization within the store to use.
     * @param username The user name to use when connecting to the store.
     * @param password The password to use when connecting to the store.
     * @param releaseStatus The key of the default release status to use when uploading a version
     * to this store.
     * @param archiveMode The key of the archive mode to use when uploading a version to this
     * store.
     * @param uploadMode The key of the upload mode to use when uploading a version to this store.
     * @param proxyHost The proxy host to use.
     * @param proxyPort The proxy port to use.
     * @param proxyUsername The proxy username to use.
     * @param proxyPassword The proxy password to use.
     */
    @DataBoundConstructor
    public Store(
            final String id,
            final String url,
            final String organization,
            final String username,
            final String password,
            final String releaseStatus,
            final String archiveMode,
            final String uploadMode,
            final String proxyHost,
            final int proxyPort,
            final String proxyUsername,
            final String proxyPassword) {

        this.setId(id);
        this.setUrl(url);

        if (StringUtils.isNotBlank(organization)) {
            this.setUsername(organization + "\\" + username);
        } else {
            this.setUsername(username);
        }

        this.setPassword(password);

        this.setReleaseStatus(releaseStatus);
        this.setArchiveMode(archiveMode);
        this.setUploadMode(uploadMode);

        this.setProxyHost(proxyHost);
        this.setProxyPort(proxyPort);

        this.setProxyUsername(proxyUsername);
        this.setProxyPassword(proxyPassword);
    }

    public Store(
            final String url,
            final String username,
            final String password,
            final String proxyHost,
            final int proxyPort,
            final String proxyUsername,
            final String proxyPassword) {
        this(null, url, null, username, password, null, null, null, proxyHost, proxyPort, proxyUsername, proxyPassword);
    }

    /**
     * Initializes a new instance of the {@link Store} class.
     * @param storeJsonObject A {@link JSONObject} used to initialize internal fields
     */
    public Store(final JSONObject storeJsonObject) {
        this.setId(storeJsonObject.optString(KEY_ID));
        this.setUrl(storeJsonObject.getString(KEY_URL));

        this.setUsername(storeJsonObject.getString(KEY_USERNAME));
        this.setPassword(storeJsonObject.getString(KEY_PASSWORD));

        this.setReleaseStatus(storeJsonObject.getString(KEY_RELEASE_STATUS));
        this.setArchiveMode(storeJsonObject.getString(KEY_ARCHIVE_MODE));
        this.setUploadMode(storeJsonObject.getString(KEY_UPLOAD_MODE));

        this.setProxyHost(storeJsonObject.getString(KEY_PROXY_HOST));
        this.setProxyPort(storeJsonObject.optInt(KEY_PROXY_PORT, 0));

        this.setProxyUsername(storeJsonObject.getString(KEY_PROXY_USERNAME));
        this.setProxyPassword(storeJsonObject.getString(KEY_PROXY_PASSWORD));
    }

    private String getId(final String id) {
        if (StringUtils.isBlank(id)) {
            return UUID.randomUUID().toString();
        }
        return id;
    }

    /**
     * @return The unique identifier for the store.
     */
    public String getId() {
        return this.getId(this.mId);
    }

    /**
     * Sets the unique identifier for the store.
     * @param id The identifier to set.
     */
    public void setId(final String id) {
        this.mId = this.getId(id);
    }

    /**
     * @return The URL to use when connecting to the store.
     */
    public String getUrl() {
        return this.mUrl;
    }

    /**
     * Sets the URL to use when connecting to the store.
     * @param url The store's API URL.
     */
    public void setUrl(final String url) {
        this.mUrl = url;
    }

    /**
     * @return The user name to use when connecting to the store.
     */
    public String getUsername() {
        return this.mUsername;
    }

    /**
     * Sets the user name to use when connecting to the store.
     * @param username The user name to use.
     */
    public void setUsername(final String username) {
        this.mUsername = username;
    }

    /**
     * @return The password to use when connecting to the store.
     */
    public String getPassword() {
        return this.mPassword;
    }

    /**
     * Sets the password to use when connecting to the store.
     * @param password The password to use.
     */
    public void setPassword(final String password) {
        this.mPassword = password;
    }

    /**
     * @return The key of the default {@link ReleaseStatus} to use when uploading a version to the
     * store.
     */
    public String getReleaseStatus() {
        return this.mReleaseStatus;
    }

    /**
     * Sets the key of the default {@link ReleaseStatus} to use when uploading a version to the
     * store.
     * @param releaseStatus The release status to use.
     */
    public void setReleaseStatus(final String releaseStatus) {
        this.mReleaseStatus = releaseStatus;
    }

    /**
     * @return The key of the default {@link ArchiveMode} to use when uploading a version to the
     * store.
     */
    public String getArchiveMode() {
        return this.mArchiveMode;
    }

    /**
     * Sets the key of the default {@link ArchiveMode} to use when uploading a version to the
     * store.
     * @param archiveMode The archive mode to use.
     */
    public void setArchiveMode(final String archiveMode) {
        this.mArchiveMode = archiveMode;
    }

    /**
     * @return The key of the default {@link UploadMode} that determines which artifacts to upload
     * to the store.
     */
    public String getUploadMode() {
        return this.mUploadMode;
    }

    /**
     * Sets the key of the default {@link UploadMode} that determines which artifacts to upload to
     * the store.
     * @param uploadMode The upload mode to use.
     */
    public void setUploadMode(final String uploadMode) {
        this.mUploadMode = uploadMode;
    }

    /**
     * @return The host name of the proxy server to use.
     */
    public String getProxyHost() {
        return this.mProxyHost;
    }

    /**
     * Sets the host name of the proxy server to use.
     * @param proxyHost A host name.
     */
    public void setProxyHost(final String proxyHost) {
        this.mProxyHost = proxyHost;
    }

    /**
     * @return The port number of the proxy server to use.
     */
    public int getProxyPort() {
        return this.mProxyPort;
    }

    /**
     * Sets the port number of the proxy server to use.
     * @param proxyPort A port number.
     */
    public void setProxyPort(final int proxyPort) {
        this.mProxyPort = proxyPort;
    }

    /**
     * @return The username to use for proxy authentication.
     */
    public String getProxyUsername() {
        return this.mProxyUsername;
    }

    /**
     * Sets the username to use for proxy authentication.
     * @param proxyUsername The username to use.
     */
    public void setProxyUsername(final String proxyUsername) {
        this.mProxyUsername = proxyUsername;
    }

    /**
     * @return The password to use for proxy authentication.
     */
    public String getProxyPassword() {
        return this.mProxyPassword;
    }

    /**
     * Sets the password to use for proxy authentication.
     * @param proxyPassword The password to use.
     */
    public void setProxyPassword(final String proxyPassword) {
        this.mProxyPassword = proxyPassword;
    }

    /**
     * @return The host component of the store's {@link #getUrl() URL}.
     */
    public String getHostName() {

        try {
            final URI uri = new URI(this.mUrl);
            return uri.getHost();

        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
        return this.mUrl;
    }

    /**
     * Converts the store to its JSON representation.
     * @return A {@link JSONObject} that represents this {@link Store}.
     */
    public JSONObject toJson() {
        final JSONObject json = new JSONObject();

        json.put(KEY_ID, this.getId(this.mId));
        json.put(KEY_URL, this.mUrl);

        json.put(KEY_USERNAME, this.mUsername);
        json.put(KEY_PASSWORD, this.mPassword);

        json.put(KEY_RELEASE_STATUS, this.mReleaseStatus);
        json.put(KEY_ARCHIVE_MODE, this.mArchiveMode);
        json.put(KEY_UPLOAD_MODE, this.mUploadMode);

        json.put(KEY_PROXY_HOST, this.mProxyHost);
        json.put(KEY_PROXY_PORT, this.mProxyPort);

        json.put(KEY_PROXY_USERNAME, this.mProxyUsername);
        json.put(KEY_PROXY_PASSWORD, this.mProxyPassword);

        return json;
    }

    @Override
    public int hashCode() {
        return this.mId.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof Store) {
            final Store other = (Store) obj;
            return StringUtils.equals(this.mId, other.mId);
        }

        return false;
    }

    @Override
    public String toString() {

        return String.format(
                Locale.ENGLISH,
                "%s - %s@%s",
                this.getHostName(),
                this.mUsername);
    }

    @Extension
    public static class StoreDescriptor extends Descriptor<Store> {

        @Override
        public String getDisplayName() {
            return "Store";
        }

        public FormValidation doCheckUrl(@QueryParameter final String value) throws IOException, ServletException {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("API URL must not be empty");
            }

            final UrlValidator validator = new UrlValidator(URL_SCHEMES);

            if (!validator.isValid(value)) {
                return FormValidation.error(
                        "API URL must have valid scheme (%s).",
                        StringUtils.join(URL_SCHEMES, ", "));
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckUsername(@QueryParameter final String value) {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("User name must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckOrganization(@QueryParameter final String value) {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.error("Organization must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckPassword(@QueryParameter final String value) {

            if (StringUtils.isEmpty(value)) {
                return FormValidation.warning("Consider using a password for security reasons");
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

        public FormValidation doTestConnection(
                @QueryParameter(Store.KEY_URL) final String url,
                @QueryParameter(Store.KEY_USERNAME) final String username,
                @QueryParameter(Store.KEY_PASSWORD) final String password,
                @QueryParameter(Store.KEY_PROXY_HOST) final String proxyHost,
                @QueryParameter(Store.KEY_PROXY_PORT) final int proxyPort,
                @QueryParameter(Store.KEY_PROXY_USERNAME) final String proxyUsername,
                @QueryParameter(Store.KEY_PROXY_PASSWORD) final String proxyPassword)
                throws IOException, ServletException {

            if (StringUtils.isEmpty(url)) {
                return FormValidation.warning("Unable to validate, the specified URL is empty.");
            }

            if (!StringUtils.isEmpty(proxyHost) && proxyPort <= 0) {
                return FormValidation.warning("Host name for proxy set, but invalid port configured.");
            }

            SessionManager sessionManager = null;
            try {
                final Store store = new Store(url, username, password, proxyHost, proxyPort, proxyUsername, proxyPassword);
                final BaseRequest request = RequestFactory.createAppStoreItemsRequest(store);

                sessionManager = new SessionManager();
                sessionManager.setProxy(proxyHost, proxyPort);
                sessionManager.setProxyCredentials(proxyUsername, proxyPassword);

                sessionManager.logIn(store);
                final ApiResponse response = sessionManager.execute(request);

                switch (response.getStatusCode()) {
                    case HttpStatus.SC_OK:
                        final ServerVersion serverVersion = sessionManager.getServerVersion();
                        return FormValidation.ok("Connection attempt successful (Relution %s)", serverVersion);

                    case HttpStatus.SC_FORBIDDEN:
                        return FormValidation.error(
                                "Connection attempt failed, authentication error, please verify credentials (%d)",
                                response.getStatusCode());

                    case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                        return FormValidation.error(
                                "Connection attempt failed, proxy authentication error, please verify proxy credentials (%d)",
                                response.getStatusCode());

                    default:
                        return FormValidation.warning(
                                "Connection attempt successful, but API call failed, is this an API URL? (%d)",
                                response.getStatusCode());
                }

            } catch (final Exception e) {
                return this.parseError(e);

            } finally {
                IOUtils.closeQuietly(sessionManager);

            }
        }

        private FormValidation parseError(final Throwable error) {

            if (ErrorType.is(error, ExecutionException.class, UnknownHostException.class)) {
                return FormValidation.error("Connection attempt failed, the specified host name is unreachable");

            } else if (ErrorType.is(error, ExecutionException.class, HttpException.class)) {
                return FormValidation.error("Connection attempt failed, the specified protocol is unsupported");

            } else if (ErrorType.is(error, IllegalArgumentException.class)) {
                return FormValidation.error("Connection attempt failed, the specified API URL is invalid");

            } else if (ErrorType.is(error, ParseException.class)) {
                return FormValidation.error("Unable to parse server response");

            }
            return FormValidation.error("Unknown error: %s", error);
        }

        public ListBoxModel doFillReleaseStatusItems() {
            final ListBoxModel items = new ListBoxModel();
            ReleaseStatus.fillListBox(items);
            return items;
        }

        public ListBoxModel doFillArchiveModeItems() {
            final ListBoxModel items = new ListBoxModel();
            ArchiveMode.fillListBox(items);
            return items;
        }

        public ListBoxModel doFillUploadModeItems() {
            final ListBoxModel items = new ListBoxModel();
            UploadMode.fillListBox(items);
            return items;
        }
    }
}
