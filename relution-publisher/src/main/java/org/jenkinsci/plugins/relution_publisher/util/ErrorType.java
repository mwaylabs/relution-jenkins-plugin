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

package org.jenkinsci.plugins.relution_publisher.util;

import org.apache.commons.lang.StringUtils;


public class ErrorType {

    public static boolean is(final Throwable error, final Class<?> errorClass, final Class<?> cause, final String... segments) {

        if (error == null) {
            return false;
        }

        if (errorClass != null && !errorClass.isInstance(error)) {
            return false;
        }

        if (cause != null && !cause.isInstance(error.getCause())) {
            return false;
        }

        if (segments.length > 0) {
            final String message = error.getMessage();

            if (StringUtils.isBlank(message)) {
                return false;
            }

            for (final String segment : segments) {
                if (!message.contains(segment)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean is(final Throwable error, final Class<?> errorClass) {
        return is(error, errorClass, null, new String[0]);
    }

    public static boolean is(final Throwable error, final Class<?> errorClass, final String... segments) {
        return is(error, errorClass, null, segments);
    }
}
