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

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;

import hudson.model.Result;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;


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
    public final static UploadMode DEFAULT = new UploadMode("DEFAULT", "(default)");

    /**
     * Versions produced by the build process should only be uploaded if they completed with
     * {@link Result#SUCCESS} (default).
     */
    public final static UploadMode SUCCESS = new UploadMode("SUCCESS", "build is successful");

    /**
     * Versions produces by the build process should also be uploaded if they complete with
     * {@link Result#UNSTABLE}.
     */
    public final static UploadMode UNSTABLE = new UploadMode("UNSTABLE", "build is successful or unstable");

    private UploadMode(final String key, final String name) {
        super(key, name);
    }

    private static Option optionOrDefault(final Store store) {
        if (store == null) {
            return DEFAULT.asOption();
        }
        final UploadMode mode = getByKey(store.getUploadMode());
        return newOption(DEFAULT, "%s* (default)", mode.name);
    }

    /**
     * Returns the upload mode associated with the specified key.
     * @param key The key for which to get the upload mode.
     * @return The {@link UploadMode} with the specified key, or {@link UploadMode#DEFAULT}
     * if the specified key matches no upload mode.
     */
    public static UploadMode getByKey(final String key) {
        if (StringUtils.equals(key, DEFAULT.key)) {
            return DEFAULT;
        } else if (StringUtils.equals(key, SUCCESS.key)) {
            return SUCCESS;
        } else if (StringUtils.equals(key, UNSTABLE.key)) {
            return UNSTABLE;
        }
        return DEFAULT;
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
     * @param store
     */
    public static void fillListBox(final ListBoxModel list, final Store store) {
        list.add(0, optionOrDefault(store));
        list.add(1, SUCCESS.asOption());
        list.add(2, UNSTABLE.asOption());
    }
}
