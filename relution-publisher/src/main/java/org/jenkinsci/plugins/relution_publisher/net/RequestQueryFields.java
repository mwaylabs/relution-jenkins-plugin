/*
 * Copyright (c) 2013 M-Way Solutions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.plugins.relution_publisher.net;

import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Represents a collection of URL encoded query parameters to be appended to a URI.
 */
public class RequestQueryFields {

    private final static String             DEFAULT_CHARSET      = "UTF-8";
    private final static String             UNSUPPORTED_ENCODING = "The configured charset is unsupported on the current platform.";

    private String                          mCharsetName         = DEFAULT_CHARSET;

    private final Map<String, List<String>> mFields              = new HashMap<String, List<String>>();
    private String                          mQuery;

    private String encode(final String value) {

        try {
            return URLEncoder.encode(value, this.mCharsetName);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(UNSUPPORTED_ENCODING, e);
        }
    }

    private void append(final StringBuilder sb, final String name, final List<String> values) {

        for (final String value : values) {
            if (sb.length() == 0) {
                sb.append('?');
            } else {
                sb.append('&');
            }
            sb.append(this.encode(name));
            sb.append('=');
            sb.append(this.encode(value));
        }
    }

    /**
     * Gets the character set used when encoding strings.
     */
    public String getCharset() {
        return this.mCharsetName;
    }

    /**
     * Specifies the character set to use when encoding strings.
     * @param charsetName The name of the character set to use.
     * @throws IllegalCharsetNameException if the specified name is illegal.
     * @throws UnsupportedEncodingException if the specified character set is unsupported.
     */
    public void setCharset(final String charsetName) throws UnsupportedEncodingException {

        if (!Charset.isSupported(charsetName)) {
            throw new UnsupportedEncodingException("The specified charset is unsupported: " + charsetName);
        }
        this.mCharsetName = charsetName;
    }

    /**
     * Adds a query parameter with the specified name and value to the query, keeping existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name has already been specified it will <b>not</b> be
     * replaced, i.e. the query can contain multiple parameters with the same name and any value
     * previously specified. To replace existing parameters with the same name use {@link #set}.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName
     * @param value
     * @see #set
     */
    public void add(final String fieldName, final String value) {

        if (fieldName == null) {
            throw new IllegalArgumentException("The specified argument cannot be null: fieldName");
        }

        if (StringUtils.isBlank(value)) {
            this.mFields.remove(fieldName);
            return;
        }

        List<String> values = this.mFields.get(fieldName);

        if (values == null) {
            values = new ArrayList<String>();
            this.mFields.put(fieldName, values);
        }

        values.add(value);
        this.mQuery = null;
    }

    /**
     * Adds a query parameter with the specified name and value to the query, keeping existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name has already been specified it will <b>not</b> be
     * replaced, i.e. the query can contain multiple parameters with the same name and any value
     * previously specified. To replace existing parameters with the same name use {@link #set}.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName
     * @param value
     * @see #set
     */
    public void add(final String fieldName, final int value) {
        this.add(fieldName, String.valueOf(value));
    }

    /**
     * Adds a query parameter with the specified name and value to the query, keeping existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name has already been specified it will <b>not</b> be
     * replaced, i.e. the query can contain multiple parameters with the same name and any value
     * previously specified. To replace existing parameters with the same name use {@link #set}.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName
     * @param value
     * @see #set
     */
    public void add(final String fieldName, final long value) {
        this.add(fieldName, String.valueOf(value));
    }

    /**
     * Adds a query parameter with the specified name and value to the query, keeping existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name has already been specified it will <b>not</b> be
     * replaced, i.e. the query can contain multiple parameters with the same name and any value
     * previously specified. To replace existing parameters with the same name use {@link #set}.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName
     * @param value
     * @see #set
     */
    public void add(final String fieldName, final double value) {
        this.add(fieldName, String.format(Locale.ENGLISH, "%1$f", value));
    }

    /**
     * Adds a query parameter with the specified name and value to the query, keeping existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name has already been specified it will <b>not</b> be
     * replaced, i.e. the query can contain multiple parameters with the same name and any value
     * previously specified. To replace existing parameters with the same name use {@link #set}.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName
     * @param value
     * @see #set
     */
    public void add(final String fieldName, final boolean value) {
        this.add(fieldName, value ? "true" : "false");
    }

    /**
     * Adds a query parameter with the specified name and value to the query, keeping existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name has already been specified it will <b>not</b> be
     * replaced, i.e. the query can contain multiple parameters with the same name and any value
     * previously specified. To replace existing parameters with the same name use {@link #set}.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName
     * @param value
     * @see #set
     */
    public void add(final String fieldName, final Date date) {
        this.add(fieldName, date != null ? date.getTime() : null);
    }

    /**
     * Adds a query parameter with the specified name and value to the query, replacing existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name already exists it will be replace, i.e. the query
     * will only contain the specified value. The keep existing parameters with the same name
     * use {@link #add} instead.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName The name of the query parameter to add.
     * @param value The value of the query parameter to add.
     * @see #add
     */
    public void set(final String fieldName, final String value) {

        if (fieldName == null) {
            throw new IllegalArgumentException("The specified argument cannot be null: fieldName");
        }

        if (StringUtils.isBlank(value)) {
            this.mFields.remove(fieldName);
            return;
        }

        final List<String> values = new ArrayList<String>();
        values.add(value);

        this.mFields.put(fieldName, values);
        this.mQuery = null;
    }

    /**
     * Adds a query parameter with the specified name and value to the query, replacing existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name already exists it will be replace, i.e. the query
     * will only contain the specified value. The keep existing parameters with the same name
     * use {@link #add} instead.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName The name of the query parameter to add.
     * @param value The value of the query parameter to add.
     * @see #add
     */
    public void set(final String fieldName, final int value) {
        this.set(fieldName, String.valueOf(value));
    }

    /**
     * Adds a query parameter with the specified name and value to the query, replacing existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name already exists it will be replace, i.e. the query
     * will only contain the specified value. The keep existing parameters with the same name
     * use {@link #add} instead.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName The name of the query parameter to add.
     * @param value The value of the query parameter to add.
     * @see #add
     */
    public void set(final String fieldName, final long value) {
        this.set(fieldName, String.valueOf(value));
    }

    /**
     * Adds a query parameter with the specified name and value to the query, replacing existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name already exists it will be replace, i.e. the query
     * will only contain the specified value. The keep existing parameters with the same name
     * use {@link #add} instead.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName The name of the query parameter to add.
     * @param value The value of the query parameter to add.
     * @see #add
     */
    public void set(final String fieldName, final double value) {
        this.set(fieldName, String.format(Locale.ENGLISH, "%1$f", value));
    }

    /**
     * Adds a query parameter with the specified name and value to the query, replacing existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name already exists it will be replace, i.e. the query
     * will only contain the specified value. The keep existing parameters with the same name
     * use {@link #add} instead.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName The name of the query parameter to add.
     * @param value The value of the query parameter to add.
     * @see #add
     */
    public void set(final String fieldName, final boolean value) {
        this.set(fieldName, value ? "true" : "false");
    }

    /**
     * Adds a query parameter with the specified name and value to the query, replacing existing
     * values with the same name.
     * <p/>
     * If another parameter with the same name already exists it will be replace, i.e. the query
     * will only contain the specified value. The keep existing parameters with the same name
     * use {@link #add} instead.
     * <p/>
     * If the specified value is {@code null} or an empty string the query parameter is removed.
     * @param fieldName The name of the query parameter to add.
     * @param value The value of the query parameter to add.
     * @see #add
     */
    public void set(final String fieldName, final Date date) {
        this.set(fieldName, date != null ? date.getTime() : null);
    }

    /**
     * Removes all query parameters with the specified name from the query.
     * @param fieldName The name of the query parameter to remove.
     */
    public void remove(final String fieldName) {

        this.mFields.remove(fieldName);
        this.mQuery = null;
    }

    /**
     * Removes the query parameter with the specified name and value from the query.
     * @param fieldName The name of the query parameter to remove.
     * @param value The value of the query parameter to remove.
     */
    public void remove(final String fieldName, final String value) {

        final List<String> values = this.mFields.get(fieldName);
        if (values != null) {
            values.remove(value);
        }
        this.mQuery = null;
    }

    /**
     * Removes all parameters from this query, leaving it empty.
     */
    public void clear() {

        this.mFields.clear();
        this.mQuery = null;
    }

    /**
     * Returns the number of parameters added to this query.
     * @return The number of parameters added to this query.
     */
    public int size() {
        return this.mFields.size();
    }

    /**
     * Returns a string representation of the query parameters represented by this instance. The
     * string is formatted so that it can be appended to a URI. Keys and values URL encoded.
     * <p/><b>Example</b>:
     * <pre>{@code ?key=value&key=value}.</pre>
     * @return The URL encoded string representation of this query that can be appended to a URI.
     */
    @Override
    public String toString() {

        if (this.mQuery == null) {
            final StringBuilder sb = new StringBuilder();

            for (final String name : this.mFields.keySet()) {
                final List<String> values = this.mFields.get(name);
                this.append(sb, name, values);
            }
            this.mQuery = sb.toString();
        }
        return this.mQuery;
    }
}
