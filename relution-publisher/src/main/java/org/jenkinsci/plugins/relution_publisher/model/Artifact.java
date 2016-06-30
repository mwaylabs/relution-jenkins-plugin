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

package org.jenkinsci.plugins.relution_publisher.model;

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.jobs.Publication;

import java.io.File;
import java.io.Serializable;

import hudson.model.Result;


/**
 * Defines the data that is required to publish a build artifact to an app store.
 */
public class Artifact implements Serializable, ResultHolder {

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

    private final Store       store;
    private final File        basePath;
    private final Publication publication;

    private Result            result;

    /**
     * Creates a new instance of the {@link Artifact} class.
     * @param store The {@link Store} to which the artifact should be published.
     * @param basePath The base path of the workspace that contains the artifact.
     * @param publication The {@link Publication} that describes the file(s) to be published.
     * @param result The {@link Result} of the build.
     */
    public Artifact(final Store store, final File basePath, final Publication publication, final Result result) {
        this.store = store;
        this.basePath = basePath;
        this.publication = publication;

        this.result = result;
    }

    /**
     * Returns a value indicating whether the artifact's result matches the specified value.
     * @param result The result to test.
     * @return {@code true} if the artifact's result is equal to {@code result}; otherwise,
     * {@code false}.
     */
    public boolean is(final Result result) {
        return this.result == result;
    }

    /**
     * @return The store to which the build artifact should be published.
     */
    public Store getStore() {
        return this.store;
    }

    /**
     * @return The base path of the workspace that contains the build artifacts.
     */
    public File getBasePath() {
        return this.basePath;
    }

    /**
     * @return The publication that defines the actual files to publish.
     */
    public Publication getPublication() {
        return this.publication;
    }

    /**
     * @return The {@link Result} of the build that produced the artifact.
     */
    @Override
    public Result getResult() {
        return this.result;
    }

    /**
     * Sets the result to the specified value.
     * @param result The {@link Result} to set.
     */
    @Override
    public void setResult(final Result result) {
        this.result = result;
    }
}
