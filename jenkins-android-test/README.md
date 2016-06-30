jenkins-android-test
====================

This is a sample Android application project that is intended to be used in combination with the relution-jenkins-plugin. The main purpose of this project is its use during development of the plugin, not the app itself.

The app can be built on a Jenkins server using the Gradle plugin for Jenkins. This provides a build artifact, which can be uploaded to a Relution store using the relution-jenkins-plugin. This allows to test the plugin's functionality without the need for an actual full-size Android project. This speeds up testing, by keeping the build time as low as possible.

Build artifacts produced by this project are versioned using Jenkins's build number. This means each artifact produced by Jenkins represents a new application version. This allows to upload each artifact without the need to remove previous build artifacts from the server (The server prevents duplicate uploads of the same version). This can be an issue when testing with a real project that uses Git tags or other means to determine the version code.