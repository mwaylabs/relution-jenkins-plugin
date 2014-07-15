/*
 * Copyright (c) 2013-2014 M-Way Solutions GmbH
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

package org.jenkinsci.plugins.relution_publisher.constants;

import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;


public abstract class Choice {

    /**
     * The key for the choice.
     */
    public final String key;

    /**
     * The display name for the choice.
     */
    public final String name;

    private Option      option;

    /**
     * Initializes a new instance of the {@link Choice} class.
     * @param key The key to use for the choice.
     * @param name The display name for the choice. 
     */
    protected Choice(final String key, final String name) {

        this.key = key;
        this.name = name;
    }

    /**
     * Converts the {@link Choice} to a a drop down item for a {@link ListBoxModel}.
     * @return An {@link Option}.
     */
    public Option asOption() {

        if (this.option == null) {
            this.option = new Option(this.name, this.key);
        }
        return this.option;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof Choice) {
            final Choice other = (Choice) obj;
            return this.key.equals(other.key);
        }

        return false;
    }
}
