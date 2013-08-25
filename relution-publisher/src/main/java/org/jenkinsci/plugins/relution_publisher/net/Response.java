/*
 * Copyright (c) 2013 M-Way Solutions GmbH
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

package org.jenkinsci.plugins.relution_publisher.net;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;


public class Response {

    private final static Charset CHARSET = Charset.forName("UTF-8");

    private final Request        mRequest;
    private HttpResponse         mResponse;

    public Response(final Request request) {
        this.mRequest = request;
    }

    protected void setHttpResponse(final HttpResponse response) {
        this.mResponse = response;
    }

    public HttpResponse getHttpResponse() {
        return this.mResponse;
    }

    public Request getRequest() {
        return this.mRequest;
    }

    public String getData() throws ParseException, IOException {

        final HttpEntity entity = this.mResponse.getEntity();
        return EntityUtils.toString(entity, CHARSET);
    }

    public int getHttpCode() {
        return this.mResponse.getStatusLine().getStatusCode();
    }
}
