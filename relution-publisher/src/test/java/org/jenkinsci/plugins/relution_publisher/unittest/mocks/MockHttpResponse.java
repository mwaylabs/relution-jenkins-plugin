
package org.jenkinsci.plugins.relution_publisher.unittest.mocks;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

import java.util.Locale;


public class MockHttpResponse implements HttpResponse {

    private StatusLine statusLine;

    public MockHttpResponse() {
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        if (this.statusLine == null) {
            return null;
        }
        return this.statusLine.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(final String name) {
        return false;
    }

    @Override
    public Header[] getHeaders(final String name) {
        return null;
    }

    @Override
    public Header getFirstHeader(final String name) {
        return null;
    }

    @Override
    public Header getLastHeader(final String name) {
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        return null;
    }

    @Override
    public void addHeader(final Header header) {
    }

    @Override
    public void addHeader(final String name, final String value) {
    }

    @Override
    public void setHeader(final Header header) {
    }

    @Override
    public void setHeader(final String name, final String value) {
    }

    @Override
    public void setHeaders(final Header[] headers) {
    }

    @Override
    public void removeHeader(final Header header) {
    }

    @Override
    public void removeHeaders(final String name) {
    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator(final String name) {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(final HttpParams params) {
    }

    @Override
    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    @Override
    public void setStatusLine(final StatusLine statusline) {
        this.statusLine = statusline;
    }

    @Override
    public void setStatusLine(final ProtocolVersion ver, final int code) {
        this.statusLine = new MockStatusLine(ver, code, null);
    }

    @Override
    public void setStatusLine(final ProtocolVersion ver, final int code, final String reason) {
        this.statusLine = new MockStatusLine(ver, code, reason);
    }

    @Override
    public void setStatusCode(final int code) throws IllegalStateException {
        throw new NotImplementedException();
    }

    @Override
    public void setReasonPhrase(final String reason) throws IllegalStateException {
        throw new NotImplementedException();
    }

    @Override
    public HttpEntity getEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEntity(final HttpEntity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLocale(final Locale loc) {
        // TODO Auto-generated method stub

    }
}
