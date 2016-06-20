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

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;
import org.jenkinsci.plugins.relution_publisher.factories.UploaderFactory;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.model.ResultHolder;
import org.jenkinsci.plugins.relution_publisher.model.ServerVersion;
import org.jenkinsci.plugins.relution_publisher.net.AuthenticatedNetwork;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;
import org.jenkinsci.plugins.relution_publisher.net.SessionManager;
import org.jenkinsci.plugins.relution_publisher.util.Builds;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import hudson.FilePath.FileCallable;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;


/**
 * Uploads build artifacts to a {@link Store} that has been specified in a Jenkins project's
 * post-build action, in the form of a {@link Publication}.
 */
public class ArtifactFileUploader implements FileCallable<Result>, ResultHolder {

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
    private static final long          serialVersionUID = 1L;

    private Result                     result;

    private final Publication          publication;
    private final Store                store;
    private final Log                  log;

    private final RequestFactory       requestFactory;
    private final AuthenticatedNetwork network;

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

        this.requestFactory = new RequestFactory();
        this.network = new SessionManager(this.requestFactory);
        this.network.setProxy(store.getProxyHost(), store.getProxyPort());
        this.network.setProxyCredentials(store.getProxyUsername(), store.getProxyPassword());
    }

    @Override
    public Result invoke(final File basePath, final VirtualChannel channel)
            throws IOException, InterruptedException {

        try {
            this.log.write(this, "Log in to server…");
            this.network.logIn(this.store);

            final ServerVersion serverVersion = this.network.getServerVersion();
            this.log.write(this, "Logged in (Relution server version %s)", serverVersion);

            final UploaderFactory factory = new UploaderFactory(this.requestFactory, this.network, this.log);
            final Uploader uploader = factory.createUploader(serverVersion, this.publication, this.store, this.result);

            this.result = uploader.publish(basePath, this.publication);

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
            this.log.write(this, "Closing connection…");
            this.network.close();
            this.log.write(this, "Connection closed");

        }
        return this.result;
    }

    @Override
    public Result getResult() {
        return this.result;
    }

    @Override
    public void setResult(final Result result) {
        this.result = result;
    }

    @Override
    public void checkRoles(final RoleChecker roleChecker) throws SecurityException {
    }
}
