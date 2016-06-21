/*
 * Copyright 2016 M-Way Solutions GmbH
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

import java.io.Serializable;
import java.util.Objects;


public class ServerVersion implements Comparable<ServerVersion>, Serializable {

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

    private final String      versionName;
    private final int[]       version;

    public ServerVersion(final String versionName) {
        this.versionName = versionName;
        this.version = this.parse(versionName);
    }

    private int[] parse(final String versionName) {
        if (StringUtils.isBlank(versionName)) {
            return new int[] {Integer.MIN_VALUE};

        }

        final String[] values = versionName.split("\\.");
        final int[] version = new int[values.length];

        for (int n = 0; n < values.length; n++) {
            try {
                final String value = values[n];
                final int number = Integer.parseInt(value);
                version[n] = number;
            } catch (final NumberFormatException e) {
                version[n] = Integer.MAX_VALUE;
            }
        }

        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.versionName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ServerVersion)) {
            return false;
        }
        final ServerVersion other = (ServerVersion) obj;
        if (!StringUtils.equals(this.versionName, other.versionName)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final ServerVersion o) {
        final int length = Math.min(this.version.length, o.version.length);

        for (int n = 0; n < length; n++) {
            final int lhs = this.version[n];
            final int rhs = o.version[n];

            if (lhs > rhs) {
                return 1;
            } else if (lhs < rhs) {
                return -1;
            }
        }

        if (this.version.length > o.version.length) {
            return 1;
        } else if (this.version.length < o.version.length) {
            return -1;
        }

        return 0;
    }

    @Override
    public String toString() {
        return this.versionName;
    }
}
