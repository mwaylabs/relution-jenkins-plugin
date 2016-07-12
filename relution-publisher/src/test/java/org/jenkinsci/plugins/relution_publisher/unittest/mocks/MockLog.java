
package org.jenkinsci.plugins.relution_publisher.unittest.mocks;

import org.jenkinsci.plugins.relution_publisher.logging.Log;

import java.io.PrintWriter;
import java.io.StringWriter;


public class MockLog implements Log {

    private static final long serialVersionUID = 1L;

    private static String valueOf(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

    @Override
    public void write() {
        System.out.println();
    }

    @Override
    public void write(final Class<?> source, final String format, final Object... args) {
        System.out.format(format, args);
        System.out.println();
    }

    @Override
    public void write(final Object source, final String format, final Object... args) {
        System.out.format(format, args);
        System.out.println();
    }

    @Override
    public void write(final Object source, final String format, final Throwable t) {
        System.out.format(format, valueOf(t));
        System.out.println();
    }
}
