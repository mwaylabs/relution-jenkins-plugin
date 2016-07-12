
package org.jenkinsci.plugins.relution_publisher.logging;

import java.io.Serializable;


public interface Log extends Serializable {

    void write();

    void write(Class<?> source, String format, Object... args);

    void write(Object source, String format, Object... args);

    void write(Object source, String format, Throwable t);
}
