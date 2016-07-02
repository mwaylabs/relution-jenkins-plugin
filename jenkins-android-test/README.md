# jenkins-android-test

This is a sample Android application project that can be used in combination with the Relution Enterprise Appstore Publisher plugin (relution-jenkins-plugin for short). The main purpose of this project is its use during development of the plugin, not the app itself.

The app can be built on a Jenkins server using the Gradle plugin for Jenkins. This provides a build artifact which can be uploaded to a Relution app store using the relution-jenkins-plugin. This allows to test the plugin's functionality without the need for an actual full-size application project. This speeds up testing, by keeping the build time as low as possible.

Build artifacts produced by this project are versioned using Jenkins's build number. This means each artifact produced by Jenkins represents a new application version. This allows to upload each artifact without the need to remove previous build artifacts from the store after each upload (each version in the store must be unique).

## Purpose

This project is intended to be used during development of the Relution Enterprise Appstore Publisher plugin. It can be used to test the plugin's functionality or debug the plugin in case of errors.

It is possible to use any other Android, iOS or Windows Phone project to do so. However, to speed up development and testing it is desirable to keep the build time of the test job as short as possible. The Jenkins build process itself is not relevant, it just needs to produce a build artifact that can be uploaded. To this end, this project has been kept as small as possible. The build times of actual application projects will typically be much slower.

It is also possible to skip the build process and just place an artifact in the workspace for the plugin to upload. However, the Relution server will reject duplicate versions during upload. This means the artifact will have to be manually removed from the app store after each upload. This project circuments this issue by using the build number as version code. This means each build produces a new version to upload.

Using this Android project for testing has the following advantages:
- Runs on any platform (Linux, Mac, Windows)
- Each build produces a new version
- Zero dependencies
- Low build time

## Prerequisites

- Maven 3

Maven is required to build and run the Jenkins plugin, not the Android project. More information: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial

- Android SDK
- (Gradle)

The Android project uses the Gradle Wrapper, so manual installation of Gradle should not be required.

- A Relution app store account

You will also need access to a Relution app store, so the plugin can upload the build artifacts somewhere. If you do not have an account, you can create a free one on https://www.relution.io. This will give you access to your own personal app store that you can use to distribute apps to up to ten users.

## Set up

### 1. Download and install the Android SDK for your platform.

https://developer.android.com/studio/index.html

The Android SDK is required for Jenkins to be able to build the Android project. The SDK is available for any major platform (Linux, Mac, Windows). To build the project, it is enough to _get just the command line tools_ (see download options). You can download Android Studio if you want, though this isn't required.

### 2. Install at least the following packages:
- Android SDK Tools (25.1.7)
- Android SDK Platform-tools (24)
- Android SDK Build-tools (23.0.3)
- SDK Platform (24)

The Android project currently uses the SDK platform version 24 and the build-tools version 23.0.3. Using a newer version should be possible and should usually require no more than an update to the project's Gradle build scripts.

### 3. Clone the plugin's source code

`git clone https://github.com/mwaylabs/relution-jenkins-plugin.git`

You can clone the source code into any directory you want. Something like `~/git` is usually a good idea. The following paths are relative to that directory.

### 4. Compile the Android project

- Change to the project's directory
 - `cd /{root}/relution-jenkins-plugin/jenkins-android-test/`
- Compile the project
 - `./gradlew assembleRelease`

If you receive any errors, make sure you've installed the Android SDK and required packages. You may also need to configure the ANDROID_HOME environment variable to point to the directory you installed the Android SDK. Make sure to read the error message returned by Gradle, it usually explains quite well what needs to be done.

### 5. Compile and run the Jenkins plugin

- Change to the plugin's directory:
 - `cd /{root}/relution-jenkins-plugin/relution-publisher/`
- Compile and run the plugin:
 - `mvn clean compile hpi:run`

If this does not work, make sure you've a current version of Maven (3.x) installed on your machine.

This will clean and compile the plugin, then start a local Jenkins instance with this plugin included. Add the paramter `package` to run the unit tests before starting Jenkins.

Wait until you see _Jenkins is fully up and running_ in the command line. You should now be able to open Jenkins in a browser:

`http://localhost:8080/jenkins`

You should see the "Welcome to Jenkins!" message

### 6. Install required Jenkins plugins

- Navigate to **Jenkins > Manage Jenkins > Manage Plugins**
- Ensure the following plugins are installed:
 - Gradle plugin
 - Git plugin
 - Environment Injector Plugin
 - (Relution Enterprise Appstore Publisher plugin)

Install any missing plugins, then restart Jenkins. The Relution plugin should be automatically installed since we used its compile process to start Jenkins.

### 7. Configure the Relution plugin

- Navigate to **Jenkins > Manage Jenkins > Configure System**
- Scroll down to _Relution Enterprise App Store Publisher plugin_
- Click on _Add store_
- Enter the URL, username and password of your Relution app store
- Click on _Test connection_

If everything went well, you should receive a success message. Do not forget to **Save** your configuration.

### 8. Create a Jenkins job for the Android project

- Navigate back to the main page
- Click on _create new jobs_
- Enter a name for the job (e.g. "Jenkins Android test")
- Choose _Freestyle project_ and click _OK_
- Under _Source Code Management_ select _Git_

If _Git_ is unavailable, make sure you've installed the _Git plugin_ and you've restarted Jenkins after the plugin was installed.

- Enter the repository URL
 - https://github.com/mwaylabs/relution-jenkins-plugin.git

- Under _Build Environment_ check _Inject environment variables to the build process_

If _Inject environment variables to the build process_ is unavailable, make sure you've installed the _Environment Injector Plugin_ and you've restarted Jenkins after the plugin was installed.

- Enter the following value in _Properties Content_
 - `ANDROID_HOME=/dir/to/android-sdk`

- Click on _Add build step_
- Select _Invoke Gradle script_

If _Invoke Gradle script_ is unavailable, make sure you've installed the _Gradle plugin_ and you've restarted Jenkins after the plugin was installed.

- Leave the _Gradle Version_ on _(Default)_
- Switch to _Use Gradle Wrapper_
- Enter the _Tasks_
 - `assembleRelease`
- Enter the _Root Build script_:
 - `jenkins-android-test`

The build script directory is required since the Android project is located in a subdirectory of the Git repository.

You should now be able to build the project (don't forget to **Save**). If the build fails check the _Console Output_ for errors and update the configuration accordingly.

### 9. Configure the job to upload artifacts

- Navigate back to the main page
- Select your build job (e.g. "Jenkins Android test")
- Click on _Configure_
- Click on _Add post-build action_
- Select _Deploy to Relution Enterprise Appstore_
- Click on _Add publication_
- Enter the name of the file(s) to deploy
 - `**/build/outputs/apk/app-release-*.*.apk`
- Select the _Store to deploy to_ (the one you configured previously)

If you run the build again Jenkins should now upload the build artifact to your configured Relution app store. Check the _Console Output_. If everything went well you should see output similar to this:

```
[ArtifactPublisher] Publishing '**/build/outputs/apk/app-release-*.*.apk' to '<username@hostname>'
[ArtifactFileUploader] Log in to server…
[ArtifactFileUploader] Logged in (Relution server version 3.44)

[SingleRequestUploader] Find artifact files to upload…
[SingleRequestUploader] Including files that match "**/build/outputs/apk/app-release-*.*.apk"
[SingleRequestUploader] Found "jenkins-android-test/app/build/outputs/apk/app-release-unsigned-relution-publisher-1.24.apk"
[SingleRequestUploader] Find change log…
[SingleRequestUploader] Filter expression is empty, no files to include
[SingleRequestUploader] No change log set

[SingleRequestUploader] Uploading jenkins-android-test/app/build/outputs/apk/app-release-unsigned-relution-publisher-1.24.apk…
[SingleRequestUploader] - Release status          : DEVELOPMENT
[SingleRequestUploader] - Archive previous version: true
[SingleRequestUploader] - Environment             : 
[SingleRequestUploader] - App                     : 1,263,954 Byte
[SingleRequestUploader] Upload completed (3.420 s, 361 KB/s)
[SingleRequestUploader] Upload completed with success (201)
[ArtifactFileUploader] Closing connection…
[ArtifactFileUploader] Connection closed
```

