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

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;

import hudson.util.ListBoxModel;


/**
 * Indicates the archive mode of a build artifact.
 * <p/>
 * The archive mode of an application defines how to handle existing versions that have the same
 * release status. By default any existing version will be replaced by the new version, moving the
 * existing version to the archive. If the archive mode is set to {@link #OVERWRITE} the existing
 * version will be removed before the new version is uploaded, essentially overwriting the existing
 * version.
 */
public final class ArchiveMode extends Choice {

    /**
     * Versions produced by the build process should use the archive mode defined in the associated
     * {@link Store}.
     */
    public final static ArchiveMode DEFAULT = new ArchiveMode("DEFAULT", "(default)");

    /**
     * Versions produced by the build process should replace any existing version, moving the
     * current version to the archive (default).
     */
    public final static ArchiveMode ARCHIVE = new ArchiveMode("ARCHIVE", "archive the previous version");

    /**
     * Versions produced by the build process should remove any existing version, essentially
     * overwriting existing versions.
     */
    public final static ArchiveMode OVERWRITE = new ArchiveMode("OVERWRITE", "overwrite the previous version");

    private ArchiveMode(final String key, final String name) {
        super(key, name);
    }

    /**
     * Adds all available {@link ArchiveMode} items to the specified list box as drop down items.
     * @param list The {@link ArchiveMode} to which the items should be added.
     */
    public static void fillListBox(final ListBoxModel list) {

        list.add(0, ARCHIVE.asOption());
        list.add(1, OVERWRITE.asOption());
    }

    /**
     * Adds all available {@link ArchiveMode} items to the specified list box as drop down items,
     * including the special status {@link #DEFAULT}. If <i>Default</i> is selected, the archive
     * mode defined in the build's {@link Store} configuration is used.
     * @param list The {@link ListBoxModel} to which the items should be added.
     */
    public static void fillListBoxWithDefault(final ListBoxModel list) {

        list.add(0, DEFAULT.asOption());
        list.add(1, ARCHIVE.asOption());
        list.add(2, OVERWRITE.asOption());
    }
}
