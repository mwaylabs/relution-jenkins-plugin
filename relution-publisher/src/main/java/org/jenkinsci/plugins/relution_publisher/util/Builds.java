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

import org.jenkinsci.plugins.relution_publisher.builder.ArtifactFileUploader;
import org.jenkinsci.plugins.relution_publisher.logging.Log;


public class Builds {

    private static Result determineResult(final Result oldResult, final Result newResult, final Log log) {
        if (severity(oldResult) == severity(newResult)) {
            return oldResult;
        }

        if (severity(oldResult) > severity(newResult)) {
            log.write(Builds.class, "Build result is %s, will not set to %s", oldResult, newResult);
            return oldResult;
        }

        log.write(Builds.class, "Changing build result from %s to %s", oldResult, newResult);
        return newResult;
    }

    public static void setResult(final AbstractBuild<?, ?> build, final Result newResult, final Log log) {
        final Result result = determineResult(build.getResult(), newResult, log);
        build.setResult(result);
    }

    public static void setResult(final ArtifactFileUploader uploader, final Result newResult, final Log log) {
        final Result result = determineResult(uploader.getResult(), newResult, log);
        uploader.setResult(result);
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
