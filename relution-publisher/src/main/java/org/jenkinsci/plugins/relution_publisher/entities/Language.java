
package org.jenkinsci.plugins.relution_publisher.entities;

/**
 * Represents a language configured on the server.
 */
public class Language extends ApiObject {

    private final String name;
    private final String locale;

    private final boolean isDefault;

    private Language() {
        this.locale = null;
        this.name = null;
        this.isDefault = false;
    }

    /**
     * Gets the name of the language.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the locale of the language.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Returns a value indicating whether the language is the server's default language.
     * @return {@code true} if this is the server's default language; otherwise, {@code false}.
     */
    public boolean isDefault() {
        return this.isDefault;
    }
}
