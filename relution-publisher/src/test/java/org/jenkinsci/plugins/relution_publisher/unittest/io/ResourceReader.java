
package org.jenkinsci.plugins.relution_publisher.unittest.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ResourceReader {

    private static final String RESOURCE_PREFIX = "";

    public String readString(final String resourceName) throws IOException {
        final String name = RESOURCE_PREFIX + resourceName;

        final ClassLoader classLoader = ResourceReader.class.getClassLoader();
        final InputStream stream = classLoader.getResourceAsStream(name);
        assertThat(stream).as("Resource stream").isNotNull();

        final StringBuilder sb = this.read(stream);

        assertThat(sb).as("Resource string").isNotNull();
        return sb.toString();
    }

    private StringBuilder read(final InputStream stream) throws IOException {

        if (stream == null) {
            return null;
        }

        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;

        final StringBuilder sb = new StringBuilder();

        try {
            reader = new InputStreamReader(stream);
            bufferedReader = new BufferedReader(reader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

        } finally {
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(stream);
        }
        return sb;
    }
}
