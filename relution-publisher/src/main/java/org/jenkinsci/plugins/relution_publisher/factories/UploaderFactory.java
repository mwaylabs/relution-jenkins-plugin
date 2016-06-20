/*
 * Copyright (c) 2013-2016 M-Way Solutions GmbH
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

package org.jenkinsci.plugins.relution_publisher.factories;

import org.jenkinsci.plugins.relution_publisher.builder.MultiRequestUploader;
import org.jenkinsci.plugins.relution_publisher.builder.Uploader;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.model.ServerVersion;
import org.jenkinsci.plugins.relution_publisher.net.Network;
import org.jenkinsci.plugins.relution_publisher.net.RequestFactory;

import java.io.Serializable;


public class UploaderFactory implements Serializable {

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
    private static final long    serialVersionUID = 1L;

    private final RequestFactory requestFactory;
    private final Network        network;
    private final Log            log;

    public UploaderFactory(final RequestFactory requestFactory, final Network network, final Log log) {
        this.requestFactory = requestFactory;
        this.network = network;
        this.log = log;
    }

    public Uploader createUploader(final ServerVersion version) {
        return new MultiRequestUploader(
                this.requestFactory,
                this.network,
                this.log);
    }
}
