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

package org.jenkinsci.plugins.relution_publisher.util;

import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;


public class UrlUtils {

    /**
     * Returns a value indicating whether the specified URL is an HTTP URL.
     * @param url The URL to test.
     * @return {@code true} if the specified URL is an HTTP URL; otherwise, {@code false}.
     */
    public static boolean isHttpUrl(final String url) {
        return StringUtils.startsWithIgnoreCase(url, "http://");
    }

    /**
     * Returns a value indicating whether the specified URL is an HTTPS URL.
     * @param url The URL to test.
     * @return {@code true} if the specified URL is an HTTPS URL; otherwise, {@code false}.
     */
    public static boolean isHttpsUrl(final String url) {
        return StringUtils.startsWithIgnoreCase(url, "https://");
    }

    /**
     * Converts the specified URI to its base form, i.e. "<i>protocol://host</i>".
     * @param uriString A {@link String} that represents the URI to transform.
     * @return The specified URI reduced to <i>protocol://host</i>, or {@code null} if the
     * specified URI could not be parsed.
     */
    public static String toBaseUrl(final String uriString) {
        try {
            final URL url = new URL(uriString);
            return url.getProtocol() + "://" + url.getHost();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes a trailing slash from the specified path.
     * @param path The path to sanitize.
     * @return The specified path, with any trailing slash removed.
     */
    public static String sanitizePath(final String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }

        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }

    /**
     * Combines the specified array of strings into a path.
     * <p>
     * If any part is the start of an absolute HTTP(S) URL the path will be reset and this part
     * will be used as the path's new starting point.
     * <p>
     * <b>Examples:</b><br>
     * { "a", "b" } --&gt; "/a/b"<br>
     * { "http://example.com", "a", "b" } --&gt; "http://example.com/a/b"<br>
     * { "http://example.com", "a", "http://test.com", "b" } --&gt; "http://test.com/b"
     * @param parts An array of strings to combine.
     * @return A path constructed from the combined parts.
     */
    public static String combine(final String... parts) {
        final StringBuilder sb = new StringBuilder();

        for (final String part : parts) {
            if (isHttpUrl(part) || isHttpsUrl(part)) {
                sb.setLength(0);
            } else if (!part.startsWith("/")) {
                sb.append("/");
            }
            final String path = sanitizePath(part);
            sb.append(path);
        }
        return sb.toString();
    }
}
