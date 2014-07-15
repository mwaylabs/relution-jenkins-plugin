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

import hudson.model.Result;
import hudson.model.AbstractBuild;

import org.jenkinsci.plugins.relution_publisher.logging.Log;


public class Builds {

    public static void set(final AbstractBuild<?, ?> build, final Result r, final Log log) {

        final Result current = build.getResult();

        if (severity(current) == severity(r)) {
            return;
        }

        if (severity(current) > severity(r)) {
            log.write(Builds.class, "Build result is %s, will not set to %s", current, r);
            return;
        }

        log.write(Builds.class, "Changing build result from %s to %s", current, r);
        build.setResult(r);
    }

    public static int severity(final Result result) {

        if (result.equals(Result.FAILURE)) {
            return 4;
        } else if (result.equals(Result.UNSTABLE)) {
            return 3;
        } else if (result.equals(Result.NOT_BUILT)) {
            return 2;
        } else if (result.equals(Result.ABORTED)) {
            return 1;
        }
        return 0;
    }
}
