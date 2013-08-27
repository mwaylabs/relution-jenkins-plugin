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

package org.jenkinsci.plugins.relution_publisher.config.job;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import org.jenkinsci.plugins.relution_publisher.builder.VersionPublisher;
import org.jenkinsci.plugins.relution_publisher.entities.Version;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;


/**
 * Publishes a {@link Version} to the Relution Enterprise Appstore using the
 * {@link VersionPublisher} to perform the actual upload of the file.
 */
public class PublicationRecorder extends Recorder {

    private final List<Publication> publications;

    @DataBoundConstructor
    public PublicationRecorder(final List<Publication> publications) {
        this.getDescriptor().setPublications(publications);
        this.publications = publications;
    }

    public List<Publication> getPublications() {
        return this.publications;
    }

    @Override
    public PublicationRecorderDescriptor getDescriptor() {
        return (PublicationRecorderDescriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {

        return true;
    }

    @Extension
    public static final class PublicationRecorderDescriptor extends BuildStepDescriptor<Publisher> {

        private List<Publication> publications;

        public PublicationRecorderDescriptor() {
            this.load();
        }

        public List<Publication> getPublications() {
            return this.publications;
        }

        public void setPublications(final List<Publication> publications) {
            this.publications = publications;
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> clazz) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Deploy to Relution Enterprise Appstore";
        }
    }
}
