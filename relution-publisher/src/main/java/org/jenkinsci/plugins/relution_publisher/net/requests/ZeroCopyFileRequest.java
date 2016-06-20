/*
 * Copyright (c) 2013-2016 M-Way Solutions GmbH
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class ZeroCopyFileRequest extends BaseRequest {

    private final List<Item>            mFiles = new ArrayList<>();
    private ZeroCopyFileRequestProducer mProducer;

    public ZeroCopyFileRequest(final String uri) {
        super(Method.POST, uri);
    }

    public void addItem(final String name, final File file) {
        Args.notNull(name, "name");
        Args.notNull(file, "file");

        final Item item = new Item(name, file);
        this.mFiles.add(item);
    }

    public List<Item> getItems() {
        return this.mFiles;
    }

    @Override
    public Future<HttpResponse> execute(final HttpAsyncClient httpClient) throws FileNotFoundException {
        final HttpAsyncResponseConsumer<HttpResponse> consumer = new BasicAsyncResponseConsumer();
        final HttpAsyncRequestProducer producer = this.getProducer();
        return httpClient.execute(producer, consumer, null);
    }

    public long getContentLength() throws FileNotFoundException {
        final ZeroCopyFileRequestProducer producer = this.getProducer();
        return producer.getContentLength();
    }

    private ZeroCopyFileRequestProducer getProducer() throws FileNotFoundException {
        if (this.mProducer == null) {
            this.mProducer = new ZeroCopyFileRequestProducer(this);
        }
        return this.mProducer;
    }

    public static class Item {

        private final String name;
        private final File   file;

        public Item(final String name, final File file) {
            this.name = name;
            this.file = file;
        }

        public String getName() {
            return this.name;
        }

        public File getFile() {
            return this.file;
        }
    }
}
