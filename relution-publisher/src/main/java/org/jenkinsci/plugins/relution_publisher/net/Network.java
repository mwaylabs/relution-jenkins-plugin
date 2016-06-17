
package org.jenkinsci.plugins.relution_publisher.net;

import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;


public interface Network extends Closeable, Serializable {

    void setProxy(String hostname, int port);

    void setProxyCredentials(String username, String password);

    ApiResponse execute(ApiRequest request, Log log) throws IOException, InterruptedException, ExecutionException;

    ApiResponse execute(ApiRequest request) throws InterruptedException, ExecutionException, IOException;
}