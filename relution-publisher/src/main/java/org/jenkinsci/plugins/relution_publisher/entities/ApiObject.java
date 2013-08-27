
package org.jenkinsci.plugins.relution_publisher.entities;

/**
 * Represents an API object returned by the Relution server.
 */
public abstract class ApiObject {

    private final String uuid;

    protected ApiObject() {

        this.uuid = null;
    }

    public String getUuid() {
        return this.uuid;
    }
}
