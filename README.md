relution-jenkins-plugin
=======================

A Jenkins plugin for the Relution Enterprise Appstore by M-Way Solutions GmbH

This plugin allows to automatically publish applications built by Jenkins to a Relution Enterprise Appstore as part of the build process.

To do so, one or more stores can be configured in Jenkin's system configuration. Each store configuration contains the API URL of the store and the user credentials required to log in to the store and upload the build artifact. A build artifact is typically an Apple iOS (.ipa) or Google Android (.apk) binary.

After at least one store configuration has been added to the configuration it is then possible to add a post-build action <i>Deploy to Relution Enterprise Appstore</i> to a project's settings. This post-build action then allows to specify the file (binary) that should be uploaded to the store and which store it should be uploaded to. If required this step can be added multiple times to upload a file to more than one store.

By default the plugin uploads applications in the state <i>Development</i>, which is typically only accessible to developers. Once a developer is satisfied with their work they can manually move the version to <i>Review</i>, using the store's web interface. After a version has passed review it can be moved to <i>Release</i> to make it available to end users.

The configuration allows to override this default behavior, uploading an application directly to <i>Review</i> or even release if an application should be available to end users directly, skipping the review process.
