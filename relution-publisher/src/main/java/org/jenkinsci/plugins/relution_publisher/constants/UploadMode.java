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

package org.jenkinsci.plugins.relution_publisher.constants;

import hudson.model.Result;
import hudson.util.ListBoxModel;

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;


/**
 * Indicates the upload mode of a build artifact.
 * <p/>
 * The upload mode of an application defines how to handle unstable builds, i.e. whether they
 * should be uploaded to the store or not.
 */
public final class UploadMode extends Choice {

    /**
     * Versions produced by the build process should use the upload mode defined in the associated
     * {@link Store}.
     */
    public final static UploadMode DEFAULT  = new UploadMode("DEFAULT", "(default)");

    /**
     * Versions produced by the build process should only be uploaded if they completed with
     * {@link Result#SUCCESS} (default).
     */
    public final static UploadMode SUCCESS  = new UploadMode("SUCCESS", "Upload successful builds only");

    /**
     * Versions produces by the build process should also be uploaded if they complete with
     * {@link Result#UNSTABLE}.
     */
    public final static UploadMode UNSTABLE = new UploadMode("UNSTABLE", "Upload even if the build is unstable");

    private UploadMode(final String key, final String name) {
        super(key, name);
    }

    /**
     * Adds all available {@link UploadMode} items to the specified list box as drop down items.
     * @param list The {@link UploadMode} to which the items should be added.
     */
    public static void fillListBox(final ListBoxModel list) {

        list.add(0, SUCCESS.asOption());
        list.add(1, UNSTABLE.asOption());
    }

    /**
     * Adds all available {@link UploadMode} items to the specified list box as drop down items,
     * including the special status {@link #DEFAULT}. If <i>Default</i> is selected, the upload
     * mode defined in the build's {@link Store} configuration is used.
     * @param list The {@link ListBoxModel} to which the items should be added.
     */
    public static void fillListBoxWithDefault(final ListBoxModel list) {

        list.add(0, DEFAULT.asOption());
        list.add(1, SUCCESS.asOption());
        list.add(2, UNSTABLE.asOption());
    }
}
