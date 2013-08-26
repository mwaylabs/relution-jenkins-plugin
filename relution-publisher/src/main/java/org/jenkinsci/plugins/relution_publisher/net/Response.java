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
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.nio.charset.Charset;


public class Response<T extends ApiResponse> {

    public final static int      ERROR_INVALID_RESPONSE = -600;

    private final static Charset CHARSET                = Charset.forName("UTF-8");

    private final Class<T>       mResponseClass;

    private int                  mStatusCode;
    private String               mRawData;
    private T                    mData;

    public Response(final Class<T> responseClass) {

        this.mResponseClass = responseClass;
    }

    protected void setHttpResponse(final HttpResponse response) {

        try {
            this.mStatusCode = response.getStatusLine().getStatusCode();

            final HttpEntity entity = response.getEntity();
            this.mRawData = EntityUtils.toString(entity, CHARSET);

            this.mData = ApiResponse.fromJson(this.mRawData, this.mResponseClass);

        } catch (final Exception e) {
            this.mStatusCode = ERROR_INVALID_RESPONSE;
        }
    }

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public String getRawData() {
        return this.mRawData;
    }

    public T getData() {
        return this.mData;
    }
}
