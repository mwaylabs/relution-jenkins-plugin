
package org.jenkinsci.plugins.relution_publisher.unittest.mocks;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;
import org.jenkinsci.plugins.relution_publisher.unittest.io.ResourceReader;

import java.io.IOException;


public class ResponseBuilder {

    public ApiResponse create(final String resourceName, final int httpStatus, final String reason) throws IOException {
        final StatusLine statusLine = new MockStatusLine(httpStatus, reason);

        final HttpResponse httpResponse = new MockHttpResponse();
        httpResponse.setStatusLine(statusLine);

        final ResourceReader reader = new ResourceReader();
        final String json = reader.readString(resourceName);

        final ApiResponse response = ApiResponse.fromJson(json);
        response.setHttpResponse(httpResponse);
        return response;
    }
}
