
package org.jenkinsci.plugins.relution_publisher.net;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpPost;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class FileRequest extends BaseFileRequestProducer {

    private final Map<String, String> mHeaders = new HashMap<String, String>();

    public FileRequest(final URI requestURI, final File file)
            throws FileNotFoundException {
        super(requestURI, file);
    }

    public FileRequest(final String requestURI, final File file)
            throws FileNotFoundException {
        super(URI.create(requestURI), file);
    }

    @Override
    protected HttpEntityEnclosingRequest createRequest(final URI requestURI, final HttpEntity entity) {

        final HttpPost httppost = new HttpPost(requestURI);
        httppost.setEntity(entity);

        for (final String name : this.mHeaders.keySet()) {
            httppost.addHeader(name, this.mHeaders.get(name));
        }

        return httppost;
    }

    public void addHeaders(final Map<String, String> headers) {
        this.mHeaders.putAll(headers);
    }
}
