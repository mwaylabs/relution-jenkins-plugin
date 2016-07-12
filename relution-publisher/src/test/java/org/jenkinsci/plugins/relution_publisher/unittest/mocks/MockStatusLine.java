
package org.jenkinsci.plugins.relution_publisher.unittest.mocks;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;


public class MockStatusLine implements StatusLine {

    private final ProtocolVersion protocolVersion;
    private final int             httpStatus;
    private final String          reason;

    public MockStatusLine(final ProtocolVersion protocolVersion, final int httpStatus, final String reason) {
        this.protocolVersion = protocolVersion;
        this.httpStatus = httpStatus;
        this.reason = reason;
    }

    public MockStatusLine(final int httpStatus, final String reason) {
        this(null, httpStatus, reason);
    }

    public MockStatusLine(final StatusLine statusLine, final int httpStatus) {
        this.protocolVersion = statusLine.getProtocolVersion();
        this.httpStatus = httpStatus;
        this.reason = statusLine.getReasonPhrase();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getStatusCode() {
        return this.httpStatus;
    }

    @Override
    public String getReasonPhrase() {
        return this.reason;
    }
}
