
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
