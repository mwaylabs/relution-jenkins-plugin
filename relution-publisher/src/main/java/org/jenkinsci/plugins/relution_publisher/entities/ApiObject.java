
package org.jenkinsci.plugins.relution_publisher.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Represents an API object returned by the Relution server.
 */
public abstract class ApiObject {

    private final static Gson GSON = new GsonBuilder().create();

    private final String      uuid;

    private transient String  s;

    protected ApiObject() {
        this.uuid = null;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {

        if (this.s == null) {
            this.s = this.toJson();
        }
        return this.s;
    }
}
