
package org.jenkinsci.plugins.relution_publisher.net.requests;

import org.apache.http.HttpResponse;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.util.Args;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Future;


public class ZeroCopyFileRequest<T> extends BaseRequest<T> {

    private final File mFile;

    public ZeroCopyFileRequest(final String uri, final File file, final Class<? extends ApiResponse<T>> responseClass) throws FileNotFoundException {
        super(Method.POST, uri, responseClass);

        Args.notNull(file, "file");
        this.mFile = file;
    }

    public File getFile() {
        return this.mFile;
    }

    @Override
    public Future<HttpResponse> execute(final HttpAsyncClient httpClient) throws FileNotFoundException {

        final HttpAsyncResponseConsumer<HttpResponse> consumer = new BasicAsyncResponseConsumer();
        final HttpAsyncRequestProducer producer = new ZeroCopyFileRequestProducer(this);

        return httpClient.execute(producer, consumer, null);
    }
}
