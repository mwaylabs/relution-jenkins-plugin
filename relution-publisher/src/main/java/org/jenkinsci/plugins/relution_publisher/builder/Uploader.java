
package org.jenkinsci.plugins.relution_publisher.builder;

import org.jenkinsci.plugins.relution_publisher.model.Artifact;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import hudson.model.Result;


public interface Uploader {

    Result publish(Artifact artifact)
            throws IOException, ExecutionException, InterruptedException;
}
