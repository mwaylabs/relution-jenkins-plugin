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

package org.jenkinsci.plugins.relution_publisher.configuration.global;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.jenkinsci.plugins.relution_publisher.constants.ArchiveMode;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.constants.UploadMode;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.RequestManager;
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
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;


/**
 * Represents a Relution Enterprise Appstore that is used as a communication endpoint by the
 * plugin. A store is uniquely identified by its URL and the credentials required
 * to connect to the store.
 * <p/>
 * Additionally it is possible to define the default {@link ReleaseStatus} to use when uploading
 * a version to this store. The default release status can be overridden on a per Jenkins project
 * basis.
 */
public class Store extends AbstractDescribableImpl<Store>implements Serializable {

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

    public final static String KEY_URL          = "url";
    public final static String KEY_ORGANIZATION = "organization";

    public final static String KEY_USERNAME = "username";
    public final static String KEY_PASSWORD = "password";

    public final static String KEY_RELEASE_STATUS = "releaseStatus";
    public final static String KEY_ARCHIVE_MODE   = "archiveMode";
    public final static String KEY_UPLOAD_MODE    = "uploadMode";

    public final static String KEY_PROXY_HOST = "proxyHost";
    public final static String KEY_PROXY_PORT = "proxyPort";

    private final static String[] URL_SCHEMES = {"http", "https"};

    private String mUrl;

    private String mOrganization;

    private String mUsername;
    private String mPassword;

    private String mReleaseStatus;
    private String mArchiveMode;
    private String mUploadMode;

    private String mProxyHost;
    private int    mProxyPort;

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
     * @param url The URL of the store.
     * @param organization The organization within the store to use.
     * @param username The user name to use when connecting to the store.
     * @param password The password to use when connecting to the store.
     * @param releaseStatus The key of the default release status to use when uploading a version
     * to this store.
     */
    @DataBoundConstructor
    public Store(
            final String url,
            final String organization,
            final String username,
            final String password,
            final String releaseStatus,
            final String archiveMode,
            final String uploadMode,
            final String proxyHost,
            final int proxyPort) {

        this.setUrl(url);
        this.setOrganization(organization);

        this.setUsername(username);
        this.setPassword(password);

        this.setReleaseStatus(releaseStatus);
        this.setArchiveMode(archiveMode);
        this.setUploadMode(uploadMode);

        this.setProxyHost(proxyHost);
        this.setProxyPort(proxyPort);
    }

    public Store(
            final String url,
            final String organization,
            final String username,
            final String password,
            final String proxyHost,
            final int proxyPort) {
        this(url, organization, username, password, null, null, null, proxyHost, proxyPort);
    }

    /**
     * Initializes a new instance of the {@link Store} class.
     * @param storeJsonObject A {@link JSONObject} used to initialize internal fields
     */
    public Store(final JSONObject storeJsonObject) {

        this.setUrl(storeJsonObject.getString(KEY_URL));
        this.setOrganization(storeJsonObject.getString(KEY_ORGANIZATION));

        this.setUsername(storeJsonObject.getString(KEY_USERNAME));
        this.setPassword(storeJsonObject.getString(KEY_PASSWORD));

        this.setReleaseStatus(storeJsonObject.getString(KEY_RELEASE_STATUS));
        this.setArchiveMode(storeJsonObject.getString(KEY_ARCHIVE_MODE));
        this.setUploadMode(storeJsonObject.getString(KEY_UPLOAD_MODE));

        this.setProxyHost(storeJsonObject.getString(KEY_PROXY_HOST));
        this.setProxyPort(storeJsonObject.optInt(KEY_PROXY_PORT, 0));
    }

    /**
     * Gets the URL to use when connecting to the store.
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
     * Gets the organization within the store to use.
     */
    public String getOrganization() {
        return this.mOrganization;
    }

    /**
     * Sets the organization within the store to use.
     * @param organization The organization to use.
     */
    public void setOrganization(final String organization) {
        this.mOrganization = organization;
    }

    /**
     * Gets the user name to use when connecting to the store.
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
     * Gets the password to use when connecting to the store.
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
     * Gets the key of the default {@link ReleaseStatus} to use when uploading a version to the
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
     * Gets the key of the default {@link ArchiveMode} to use when uploading a version to the
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
     * Gets the key of the default {@link UploadMode} that determines which artifacts to upload to
     * the store.
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
     * Gets the host name of the proxy server to use.
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
     * Gets the port number of the proxy server to use.
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
     * Gets the host component of the store's {@link #getUrl() URL}.
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
     * Gets an authorization token that can be used to authenticate with the store.
     */
    public String getAuthorizationToken() {

        final String authorization = this.mUsername + ":" + this.mOrganization + ":" + this.mPassword;
        return Base64.encodeBase64String(authorization.getBytes());
    }

    /**
     * Converts the store to its JSON representation.
     * @return A {@link JSONObject} that represents this {@link Store}.
     */
    public JSONObject toJson() {

        final JSONObject json = new JSONObject();

        json.put(KEY_URL, this.mUrl);
        json.put(KEY_ORGANIZATION, this.mOrganization);

        json.put(KEY_USERNAME, this.mUsername);
        json.put(KEY_PASSWORD, this.mPassword);

        json.put(KEY_RELEASE_STATUS, this.mReleaseStatus);
        json.put(KEY_ARCHIVE_MODE, this.mArchiveMode);
        json.put(KEY_UPLOAD_MODE, this.mUploadMode);

        json.put(KEY_PROXY_HOST, this.mProxyHost);
        json.put(KEY_PROXY_PORT, this.mProxyPort);

        return json;
    }

    /**
     * Gets the unique identifier for the {@link Store}.
     */
    public String getIdentifier() {

        return String.format(
                "%s:%s:%s",
                this.mUsername,
                this.mOrganization,
                this.mUrl);
    }

    @Override
    public int hashCode() {

        final int a = (this.mUrl != null) ? this.mUrl.hashCode() : 0;
        final int b = (this.mOrganization != null) ? this.mOrganization.hashCode() : 0;
        final int c = (this.mUsername != null) ? this.mUsername.hashCode() : 0;

        return a ^ b ^ c;
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
            return StringUtils.equals(this.mUrl, other.mUrl)
                    && StringUtils.equals(this.mOrganization, other.mOrganization)
                    && StringUtils.equals(this.mUsername, other.mUsername);
        }

        return false;
    }

    @Override
    public String toString() {

        return String.format(
                Locale.ENGLISH,
                "%s - %s@%s",
                this.getHostName(),
                this.mUsername,
                this.mOrganization);
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
                @QueryParameter(Store.KEY_ORGANIZATION) final String organization,
                @QueryParameter(Store.KEY_PASSWORD) final String password,
                @QueryParameter(Store.KEY_PROXY_HOST) final String proxyHost,
                @QueryParameter(Store.KEY_PROXY_PORT) final int proxyPort)
                        throws IOException, ServletException {

            if (StringUtils.isEmpty(url)) {
                return FormValidation.warning("Unable to validate, the specified URL is empty.");
            }

            if (!StringUtils.isEmpty(proxyHost) && proxyPort <= 0) {
                return FormValidation.warning("Host name for proxy set, but invalid port configured.");
            }

            try {
                final Store store = new Store(url, organization, username, password, proxyHost, proxyPort);
                final BaseRequest<?> request = RequestFactory.createAppStoreItemsRequest(store);

                final RequestManager requestManager = new RequestManager();
                requestManager.setProxy(proxyHost, proxyPort);

                final ApiResponse<?> response = requestManager.execute(request);

                switch (response.getStatusCode()) {
                    case HttpStatus.SC_OK:
                        return FormValidation.ok("Connection attempt successful");

                    case HttpStatus.SC_FORBIDDEN:
                        return FormValidation.error(
                                "Connection attempt failed, authentication error, please verify credentials (%d)",
                                response.getStatusCode());

                    default:
                        return FormValidation.warning(
                                "Connection attempt successful, but API call failed, is this an API URL? (%d)",
                                response.getStatusCode());
                }

            } catch (final Exception e) {
                return this.parseError(e);

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
