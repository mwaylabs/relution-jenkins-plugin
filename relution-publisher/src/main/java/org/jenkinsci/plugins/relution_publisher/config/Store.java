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

package org.jenkinsci.plugins.relution_publisher.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.jenkinsci.plugins.relution_publisher.constants.ReleaseStatus;
import org.jenkinsci.plugins.relution_publisher.net.Request;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.Response;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

import javax.servlet.ServletException;


/**
 * Represents a Relution Enterprise Appstore that is used as a communication endpoint by the
 * plugin. A store is uniquely identified by its URL and the credentials required
 * to connect to the store.
 * <p/>
 * Additionally it is possible to define the default {@link ReleaseStatus} to use when uploading
 * a version to this store. The default release status can be overridden on a per Jenkins project
 * basis.
 */
public class Store extends AbstractDescribableImpl<Store> {

    public final static String    KEY_URL            = "url";
    public final static String    KEY_ORGANIZATION   = "organization";

    public final static String    KEY_USERNAME       = "username";
    public final static String    KEY_PASSWORD       = "password";

    public final static String    KEY_RELEASE_STATUS = "releaseStatus";

    private final static String[] URL_SCHEMES        = {"http", "https"};

    private String                mUrl;

    private String                mOrganization;

    private String                mUsername;
    private String                mPassword;

    private String                mReleaseStatus;

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
    public Store(final String url, final String organization, final String username, final String password, final String releaseStatus) {

        this.setUrl(url);
        this.setOrganization(organization);

        this.setUsername(username);
        this.setPassword(password);

        this.setReleaseStatus(releaseStatus);
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

        return json;
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

        public FormValidation doTestConnection(
                @QueryParameter(Store.KEY_URL) final String url,
                @QueryParameter(Store.KEY_USERNAME) final String username,
                @QueryParameter(Store.KEY_ORGANIZATION) final String organization,
                @QueryParameter(Store.KEY_PASSWORD) final String password)
                throws IOException, ServletException {

            if (StringUtils.isEmpty(url)) {
                return FormValidation.warning("Unable to validate, the specified URL is empty.");
            }

            final Store store = new Store(url, organization, username, password, null);
            final Request request = RequestFactory.createAppStoreItemsRequest(store);

            try {
                final Response response = request.execute();
                final int code = response.getHttpCode();

                switch (code) {
                    case HttpStatus.SC_OK:
                        return FormValidation.ok("Connection attempt completed successfully");

                    case HttpStatus.SC_FORBIDDEN:
                        return FormValidation.error("Connection attempt failed, authentication error, please verify credentials (%d)", code);

                    default:
                        return FormValidation.warning("Connection attempt successful, but API call failed, is this an API URL? (%d)", code);
                }

            } catch (final UnknownHostException e) {
                return FormValidation.error("Connection attempt failed, the specified host name is unreachable");

            } catch (final ParseException e) {
                return FormValidation.error("Unable to parse server response");

            } catch (final URISyntaxException e) {
                return FormValidation.error("The specified URL is invalid (syntax error)");

            } catch (final Exception e) {
                return FormValidation.error("Unknown error");

            }
        }

        public ListBoxModel doFillReleaseStatusItems() {
            final ListBoxModel items = new ListBoxModel();
            ReleaseStatus.fillListBox(items);
            return items;
        }
    }
}
