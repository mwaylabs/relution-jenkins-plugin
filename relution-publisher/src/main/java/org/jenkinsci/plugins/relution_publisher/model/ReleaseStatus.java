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

package org.jenkinsci.plugins.relution_publisher.model;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;

import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;


/**
 * Indicates the release status of a build artifact.
 * <p>
 * The release status of an application defines who will be able to access the application. A
 * version with status {@code Development} will be shown as a development version and is
 * usually only available to developers. A version uploaded to {@code Review} is available
 * to reviewers, while a version uploaded as {@code Release} is available to end users.
 * <p>
 * By default applications are be uploaded to {@code Development} and must be manually
 * moved to {@code Review} by a developer, to make this version available for review. The
 * version is then manually moved to {@code Release} by a reviewer if the version has passed
 * review and should replace the currently released version.
 * <p>
 * Changing the release status for an application allows to skip parts of this manual process,
 * i.e. if the application is not reviewed/tested by a human.
 */
public final class ReleaseStatus extends Choice {

    /**
     * Versions produced by the build process should use the release status defined in the
     * associated {@link Store}.
     */
    public final static ReleaseStatus DEFAULT     = new ReleaseStatus("DEFAULT", "(default)");

    /**
     * Versions produced by the build process should be uploaded to {@code Development}.
     */
    public final static ReleaseStatus DEVELOPMENT = new ReleaseStatus("DEVELOPMENT", "Development");

    /**
     * Versions produces by the build process should be uploaded to {@code Review}
     */
    public final static ReleaseStatus REVIEW      = new ReleaseStatus("REVIEW", "Review");

    /**
     * Versions produced by the build process should be uploaded to {@code Release}
     */
    public final static ReleaseStatus RELEASE     = new ReleaseStatus("RELEASE", "Release");

    /**
     * Initializes a new instance of the {@link ReleaseStatus} class.
     * @param key The key to use for the release status.
     * @param name The display name for the release status.
     */
    private ReleaseStatus(final String key, final String name) {
        super(key, name);
    }

    private static Option optionOrDefault(final Store store) {
        if (store == null) {
            return DEFAULT.asOption();
        }
        final ReleaseStatus status = getByKey(store.getReleaseStatus());
        return newOption(DEFAULT, "%s* (default)", status.name);
    }

    /**
     * Returns the release status associated with the specified key.
     * @param key The key for which to get the release status.
     * @return The {@link ReleaseStatus} with the specified key, or {@link ReleaseStatus#DEFAULT}
     * if the specified key matches no release status.
     */
    public static ReleaseStatus getByKey(final String key) {
        if (StringUtils.equals(key, DEFAULT.key)) {
            return DEFAULT;
        } else if (StringUtils.equals(key, DEVELOPMENT.key)) {
            return DEVELOPMENT;
        } else if (StringUtils.equals(key, REVIEW.key)) {
            return REVIEW;
        } else if (StringUtils.equals(key, RELEASE.key)) {
            return RELEASE;
        }
        return DEFAULT;
    }

    /**
     * Adds all available {@link ReleaseStatus} items to the specified list box as drop down items.
     * @param list The {@link ListBoxModel} to which the items should be added.
     */
    public static void fillListBox(final ListBoxModel list) {
        list.add(0, DEVELOPMENT.asOption());
        list.add(1, REVIEW.asOption());
        list.add(2, RELEASE.asOption());
    }

    /**
     * Adds all available {@link ReleaseStatus} items to the specified list box as drop down items,
     * including the special status {@link #DEFAULT}. If <i>Default</i> is selected, the release
     * status defined in the build's {@link Store} configuration is used.
     * @param list The {@link ListBoxModel} to which the items should be added.
     * @param store The {@link Store} that defines the default option.
     */
    public static void fillList(final ListBoxModel list, final Store store) {
        list.add(0, optionOrDefault(store));
        list.add(1, DEVELOPMENT.asOption());
        list.add(2, REVIEW.asOption());
        list.add(3, RELEASE.asOption());
    }
}
