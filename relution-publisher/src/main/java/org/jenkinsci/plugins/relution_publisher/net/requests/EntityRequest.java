
package org.jenkinsci.plugins.relution_publisher.net.requests;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.nio.client.HttpAsyncClient;
import org.jenkinsci.plugins.relution_publisher.net.responses.ApiResponse;

import java.util.concurrent.Future;


public class EntityRequest<T> extends BaseRequest<T> {

    private HttpEntity mHttpEntity;

    public EntityRequest(final Method method, final String uri, final Class<? extends ApiResponse<T>> responseClass) {
        super(method, uri, responseClass);
    }

    private HttpUriRequest createHttpRequest(final Method method, final String uri, final HttpEntity entity) {

        switch (method) {
            default:
            case GET:
                return new HttpGet(uri);

            case POST:
                final HttpPost post = new HttpPost(uri);
                if (entity != null) {
                    post.setEntity(entity);
                }
                return post;

            case PUT:
                final HttpPut put = new HttpPut(uri);
                if (entity != null) {
                    put.setEntity(entity);
                }
                return put;

            case DELETE:
                return new HttpDelete(uri);
        }
    }

    private HttpUriRequest createRequest() {

        final HttpUriRequest request = this.createHttpRequest(this.getMethod(), this.getUri(), this.mHttpEntity);
        this.addHeaders(request);
        return request;
    }

    public HttpEntity getEntity() {
        return this.mHttpEntity;
    }

    public void setEntity(final HttpEntity entity) {
        this.mHttpEntity = entity;
    }

    @Override
    public Future<HttpResponse> execute(final HttpAsyncClient httpClient) {

        final HttpUriRequest request = this.createRequest();
        return httpClient.execute(request, null);
    }
}
