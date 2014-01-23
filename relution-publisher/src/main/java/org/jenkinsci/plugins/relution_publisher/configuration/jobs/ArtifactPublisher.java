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

package org.jenkinsci.plugins.relution_publisher.configuration.jobs;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.relution_publisher.builder.ArtifactFileUploader;
import org.jenkinsci.plugins.relution_publisher.configuration.global.Store;
import org.jenkinsci.plugins.relution_publisher.configuration.global.StoreConfiguration;
import org.jenkinsci.plugins.relution_publisher.constants.UploadMode;
import org.jenkinsci.plugins.relution_publisher.entities.Version;
import org.jenkinsci.plugins.relution_publisher.logging.Log;
import org.jenkinsci.plugins.relution_publisher.util.Builds;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;


/**
 * Publishes a {@link Version} to the Relution Enterprise Appstore using the
 * {@link ArtifactFileUploader} to perform the actual upload of the file.
 */
public class ArtifactPublisher extends Recorder {

    private final List<Publication> publications;

    @DataBoundConstructor
    public ArtifactPublisher(final List<Publication> publications) {
        this.getDescriptor().setPublications(publications);
        this.publications = publications;
    }

    public List<Publication> getPublications() {
        return this.publications;
    }

    @Override
    public ArtifactPublisherDescriptor getDescriptor() {
        return (ArtifactPublisherDescriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {

        final Log log = new Log(listener.getLogger());
        log.write();

        if (this.publications == null) {
            log.write(this, "Skipped, no publications configured");
            Builds.set(build, Result.UNSTABLE, log);
            return true;
        }

        final StoreConfiguration configuration = this.getDescriptor().getGlobalConfiguration();

        for (final Publication publication : this.publications) {

            final Store store = configuration.getStore(publication.getStoreId());
            this.publish(build, publication, store, log);
            log.write();
        }

        return true;
    }

    private void publish(final AbstractBuild<?, ?> build, final Publication publication, final Store store, final Log log)
            throws IOException, InterruptedException {

        if (store == null) {
            log.write(
                    this,
                    "The store configured for '%s' no longer exists, please verify your configuration.",
                    publication.getArtifactPath());

            Builds.set(build, Result.UNSTABLE, log);
            return;
        }

        if (!this.shouldPublish(build, publication, store, log)) {
            log.write(this, "Not publishing to '%s' because result of build was %s", store, build.getResult());
            return;
        }

        final ArtifactFileUploader publisher = new ArtifactFileUploader(build, publication, store, log);

        log.write(this, "Publishing '%s' to '%s'", publication.getArtifactPath(), store.toString());
        build.getWorkspace().act(publisher);
    }

    private boolean shouldPublish(final AbstractBuild<?, ?> build, final Publication publication, final Store store, final Log log) {

        if (build.getResult() == Result.SUCCESS) {
            return true;
        }

        final String key = !publication.usesDefaultUploadMode()
                ? publication.getUploadMode()
                : store.getUploadMode();

        if (build.getResult() == Result.UNSTABLE && StringUtils.equals(key, UploadMode.UNSTABLE.key)) {
            log.write(this, "Will upload build with result %s, as configured", build.getResult());
            return true;
        }

        return false;
    }

    @Extension
    public static final class ArtifactPublisherDescriptor extends BuildStepDescriptor<Publisher> {

        @Inject
        private StoreConfiguration globalConfiguration;

        private List<Publication>  publications;

        public ArtifactPublisherDescriptor() {
            this.load();
        }

        public StoreConfiguration getGlobalConfiguration() {
            return this.globalConfiguration;
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
