
package org.jenkinsci.plugins.relution_publisher.net;

import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.model.ServerVersion;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public interface AuthenticatedNetwork extends Network {

    void logIn(Store store) throws InterruptedException, ExecutionException, IOException;

    boolean logOut();

    ServerVersion getServerVersion();
}
