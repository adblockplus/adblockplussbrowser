Adblock Plus for Samsung Internet
=================================

Adblock Plus for Samsung Internet is an application that works as a provider of Adblock Plus filters list for [Samsung Internet Browser][5]. The application is built by following [ad blockers development guide][6] from Samsung.

Building with Gradle
--------------------

#### Requirements

- [Android SDK][2]
- Android Build Tools 30.0.3
- JDK 8 or above

#### Building

- In the root dir, create a `local.properties` file and set the `sdk.dir=/your/path/here`
- Add your [google-services.json][4] file to app/ directory 
- From the root dir, run `./gradlew yourBuildVariant`. This will generate an .apk file in the `adblockplussbrowser/app/build/outputs/apk/yourBuildVariant/debug` directory.

Importing into Android Studio
-----------------------------

#### Requirements

- [Android SDK][2]
- [Android Studio][3]
- Android Build Tools 30.0.3
- We're compiling against Android API 30, so make sure you have that one
  installed.
- JDK 8 or above

#### Importing

- Open Android Studio and select *Open an existing Android Studio Project*, then navigate to the `adblockplussbrowser` directory. 
- Choose your build variant.

Application Architecture
------------------------
Adblock Plus for Samsung Internet has been built following Clean Architecture Principle, [Repository Pattern][7], [MVVM Architecture][8] in the presentation layer as well as jetpack components.

#### The app is comprised of the following modules:
- app module contains the `Application` class, the launcher `Activity`, and the main `Activity` with the application navigation graph;
- analytics module contains the functionality of sharing anonymized user tracking;
- base module contains utility and helper classes/functions, base models, and interfaces that are available to all other modules. This is a self-contained module that shouldn't depend on any other;
- onboarding module contains all the Onboarding UI code;
- preferences module contains all the UI code for user preferences. It communicates with the settings module, where the Model lives, and with the core via the `SubscriptionsManager` interface (from the base module);
- settings module contains the Model for the user preferences, the repository, and data sources for user preferences and configurations;
- core module listens to changes in settings and is responsible for download/update of the filters lists accordingly. It is also responsible for schedule and manages automatic updates and provides a unified filters list file to the Samsung Internet browser. This update mechanism is described below.

Subscriptions Updates
---------------------

#### Manual Update/Force Refresh (Update now)
When the user is using the _Update now_ feature, **all** the _Subscriptions_ are downloaded, even if the user had a fresh version of any of them.

#### Periodic Updates (Automatic updates)
The `UpdateSubscriptionsWorker` is scheduled to run at "no less than 6 hours" intervals. The system can delay the worker based on Connectivity criteria, battery status, etc.

When doing a periodic update, each active _Subscription_ is checked for expiration based on the current connection type:
- on **unmetered** connections (Wifi) if the last successful download occurred more than 24 hours ago or the file doesn't exist on the filesystem, it is considered **expired**;
- on **metered** connections (3g/4g/5g) if the last successful download occurred more than 3 days ago or the file doesn't exist on the filesystem, it is considered **expired**;
- if the _Subscription_ is expired a new version is downloaded (respecting `If-Modified-Since` and `If-None-Match` headers);
- if the _Subscription_ is not expired and the file still exists then the current file is used.

#### Configurations changes
When the user adds or removes a _Subscription_, adds/removes domains to the allow/block lists, or changes the Acceptable Ads setting, a new `UpdateSubscriptionsWorker` is fired to run immediately.
Configuration changes are debounced by 500ms, so if the user quickly changes more than one setting, they will be combined in just one Worker. Otherwise, a new worker will be enqueued for each setting change.

#### Automatic update config changed
When the Automatic update configuration is changed, a new worker is scheduled to run every "at least" 6 hours interval. The System can delay the worker based on System constraints.
If the Automatic update setting is configured to **Wi-Fi Only**, the worker will run after the 6 hours interval only if the device has an unmetered connection available. If the configuration is set to **Always**, the worker will run on any working network connection.

### No configuration changed since the last update
The update is skipped if all of the following criteria are met:
- there are no changes on Active Subscriptions, allow/block lists, and Acceptable Ads Status;
- it is not a periodic or manual update.

### Adding/Removing domains from the allowlist
If the only change is on the allow/block lists we simply check if the filters file for every active subscription is still present on the filesystem. If a file still exists - it is used, no matter how long ago it was last fetched. If a file is missing - the subscription is downloaded again.

### Adding/Removing Subscriptions, changing Acceptable Ads setting
When a new _Subscription_ is added, that file is reused instead of downloading it again if the filter list file already exists and the file was downloaded less than 1 hour ago. If the last successful download was completed more than 1 hour ago then the _Subscription_ is downloaded again (respecting `If-Modified-Since` and `If-None-Match` headers).
Other active _Subscriptions_ are checked only for the existence of the filter list file. The same applies to _Acceptable Ads Subscription_, which is treated like any other _Subscription_ internally.

### Failures
If a subscription fails to download, a previously downloaded version will be used if available. Otherwise, the resulting Filters list file will be created without this subscription.
When a worker fails to update a subscription it is marked on a `Retry` state and will be retried 4 times with an exponential backoff strategy.

Testing
-------
To test the whole functionality of the application you will need an Android device with Android 5.0+ (Lollipop) and Samsung Internet 5.0+.

Emulators won't help you here, unfortunately.

Continuous Integration
----------------------
Every time a new commit is pushed to any branch CI is building release version of build variant `worldAbpRelease`.

[1]: https://developer.samsung.com/internet/android/adblockers-guide.html
[2]: https://developer.android.com/studio/command-line/sdkmanager
[3]: https://developer.android.com/studio/index.html
[4]: https://developers.google.com/android/guides/google-services-plugin#adding_the_json_file
[5]: https://play.google.com/store/apps/details?id=com.sec.android.app.sbrowser
[6]: https://developer.samsung.com/internet/android/adblockers-guide.html
[7]: https://developer.android.com/topic/libraries/architecture/images/final-architecture.png
[8]: https://developer.android.com/topic/libraries/architecture/viewmodel
