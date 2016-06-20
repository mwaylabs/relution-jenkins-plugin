
package org.jenkinsci.plugins.relution_publisher.model;

import hudson.model.Result;


/**
 * Interface definition for a class that holds a {@link Result}.
 */
public interface ResultHolder {

    /**
     * @return The {@link Result}.
     */
    Result getResult();

    /**
     * Sets the result to the specified value.
     * @param result The {@link Result} to set.
     */
    void setResult(final Result result);
}
