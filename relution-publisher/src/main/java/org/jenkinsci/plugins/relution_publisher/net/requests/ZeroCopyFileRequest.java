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
