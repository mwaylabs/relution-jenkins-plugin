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

package org.jenkinsci.plugins.relution_publisher.logging;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import hudson.model.BuildListener;


public class Log implements Serializable {

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

    private final BuildListener listener;

    public Log(final BuildListener listener) {
        this.listener = listener;
    }

    private static String valueOf(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

    public void write() {
        this.listener.getLogger().println();
    }

    public void write(final Class<?> source, final String format, final Object... args) {
        final String message = String.format(
                "[%s] %s",
                source.getSimpleName(),
                String.format(format, args));

        this.listener.getLogger().println(message);
    }

    public void write(final Object source, final String format, final Object... args) {
        this.write(source.getClass(), format, args);
    }

    public void write(final Object source, final String format, final Throwable t) {
        this.write(source.getClass(), format, valueOf(t));
    }
}
