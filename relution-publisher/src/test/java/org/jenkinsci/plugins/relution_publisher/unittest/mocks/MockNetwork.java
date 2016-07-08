
package org.jenkinsci.plugins.relution_publisher.unittest.mocks;

import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.net.Network;
import org.jenkinsci.plugins.relution_publisher.net.requests.ApiRequest;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MockNetwork implements Network {

    private static final long       serialVersionUID = 1L;

    private int                     requestCount;
    private final List<ApiResponse> responses        = new ArrayList<>();

    public void add(final ApiResponse response) {
        this.responses.add(response);
    }

    @Override
    public void setProxy(final String hostname, final int port) {
        // Do nothing
    }

    @Override
    public void setProxyCredentials(final String username, final String password) {
        // Do nothing
    }

    @Override
    public ApiResponse execute(final ApiRequest request, final Log log) throws IOException, InterruptedException, ExecutionException {
        if (this.requestCount >= this.responses.size()) {
            return null;
        }
        return this.responses.get(this.requestCount++);
    }

    @Override
    public ApiResponse execute(final ApiRequest request) throws InterruptedException, ExecutionException, IOException {
        return this.execute(request, null);
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }
}
