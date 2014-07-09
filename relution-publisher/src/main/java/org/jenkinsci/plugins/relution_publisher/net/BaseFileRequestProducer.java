
package org.jenkinsci.plugins.relution_publisher.net;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.ContentEncoderChannel;
import org.apache.http.nio.FileContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.tika.Tika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.UUID;


public abstract class BaseFileRequestProducer implements HttpAsyncRequestProducer {

    private final static String    CHARSET_NAME                     = "UTF-8";
    private final static Charset   CHARSET                          = Charset.forName(CHARSET_NAME);

    private final static String    CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data; boundary=%s";
    private final static String    CRLF                             = "\r\n";

    private final URI              requestURI;
    private final File             file;
    private final RandomAccessFile accessfile;

    private FileChannel            fileChannel;
    private long                   indexFile                        = -1;

    private byte[]                 header;
    private int                    headerIndex;

    private byte[]                 footer;
    private int                    footerIndex;

    private final String           multipartBoundary                = UUID.randomUUID().toString();

    protected BaseFileRequestProducer(final URI requestURI, final File file)
            throws FileNotFoundException {

        Args.notNull(requestURI, "Request URI");
        Args.notNull(file, "Source file");

        this.requestURI = requestURI;
        this.file = file;
        this.accessfile = new RandomAccessFile(file, "r");
    }

    private void closeChannel() throws IOException {
        if (this.fileChannel != null) {
            this.fileChannel.close();
            this.fileChannel = null;
        }
    }

    private String getContentType(final File file) {

        try {
            final Tika tika = new Tika();
            return tika.detect(file);

        } catch (final IOException e) {
            return ContentType.DEFAULT_BINARY.toString();
        }
    }

    private byte[] getHeader() {

        if (this.header == null) {
            final StringBuilder sb = new StringBuilder();
            this.writeln(sb, "--%s", this.multipartBoundary);
            this.writeln(sb, "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", "file", this.file.getName());

            final String contentType = this.getContentType(this.file);
            this.writeln(sb, "Content-Type: %s", contentType);

            this.writeln(sb, "Content-Transfer-Encoding: binary");
            this.writeln(sb);

            final String value = sb.toString();
            this.header = value.getBytes(CHARSET);
        }
        return this.header;
    }

    private byte[] getFooter() {

        if (this.footer == null) {
            final StringBuilder sb = new StringBuilder();

            this.writeln(sb);
            this.writeln(sb, "--%s--", this.multipartBoundary);

            final String value = sb.toString();
            this.footer = value.getBytes(CHARSET);
        }
        return this.footer;
    }

    private boolean writeHeader(final ContentEncoder encoder, final IOControl ioctrl) throws IOException {

        final byte[] array = this.getHeader();

        if (this.headerIndex >= array.length) {
            return true;
        }

        final int length = array.length - this.headerIndex;
        final ByteBuffer buffer = ByteBuffer.wrap(array, this.headerIndex, length);
        this.headerIndex += encoder.write(buffer);

        return false;
    }

    private boolean writeFooter(final ContentEncoder encoder, final IOControl ioctrl) throws IOException {

        final byte[] array = this.getFooter();

        if (this.footerIndex >= array.length) {
            return true;
        }

        final int length = array.length - this.footerIndex;
        final ByteBuffer buffer = ByteBuffer.wrap(array, this.footerIndex, length);
        this.footerIndex += encoder.write(buffer);

        return false;
    }

    private void writeln(final StringBuilder sb, final String value, final Object... args) {

        final String data = String.format(value, args);

        sb.append(data);
        sb.append(CRLF);
    }

    private void writeln(final StringBuilder sb) {
        sb.append(CRLF);
    }

    protected abstract HttpEntityEnclosingRequest createRequest(final URI requestURI, final HttpEntity entity);

    public String getContentType() {
        return String.format(CONTENT_TYPE_MULTIPART_FORM_DATA, this.multipartBoundary);
    }

    public long getContentLength() {
        final byte[] header = this.getHeader();
        final byte[] footer = this.getFooter();

        return header.length + this.file.length() + footer.length;
    }

    @Override
    public HttpRequest generateRequest() throws IOException, HttpException {
        final BasicHttpEntity entity = new BasicHttpEntity();

        entity.setContentLength(this.getContentLength());
        entity.setContentType(this.getContentType());
        entity.setChunked(false);

        return this.createRequest(this.requestURI, entity);
    }

    @Override
    public synchronized HttpHost getTarget() {
        return URIUtils.extractHost(this.requestURI);
    }

    @Override
    public synchronized void produceContent(final ContentEncoder encoder, final IOControl ioctrl)
            throws IOException {

        if (!this.writeHeader(encoder, ioctrl)) {
            return;
        }

        if (this.fileChannel == null) {
            this.fileChannel = this.accessfile.getChannel();
            this.indexFile = 0;
        }

        final long transferred;

        if (encoder instanceof FileContentEncoder) {
            transferred = ((FileContentEncoder) encoder).transfer(this.fileChannel, this.indexFile, Integer.MAX_VALUE);

        } else {
            transferred = this.fileChannel.transferTo(this.indexFile, Integer.MAX_VALUE, new ContentEncoderChannel(encoder));

        }

        if (transferred > 0) {
            this.indexFile += transferred;
        }

        if (this.indexFile >= this.fileChannel.size()) {
            if (this.writeFooter(encoder, ioctrl)) {
                this.closeChannel();
                encoder.complete();
            }
        }
    }

    @Override
    public void requestCompleted(final HttpContext context) {
    }

    @Override
    public void failed(final Exception ex) {
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public synchronized void resetRequest() throws IOException {
        this.headerIndex = 0;
        this.footerIndex = 0;
        this.closeChannel();
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            this.accessfile.close();
        } catch (final IOException ignore) {
        }
    }
}
