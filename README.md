Adblock Plus for Samsung Internet
=================================

Adblock Plus for Samsung Internet is an application that works as a provider of the Adblock Plus filters list for [Samsung Internet Browser][5]. It is built by following the [ad blockers development guide][6] from Samsung.
It requires [Android 5.0][9] and up.

Building with Gradle
--------------------

#### Requirements

- [Android SDK][2]
- Android Build Tools 30.0.3
- JDK 11 or above

#### Building

- In the root dir, create a `local.properties` file and set the `sdk.dir=/your/path/here`
- Add your [google-services.json][4] file to `app/` directory
- From the root dir, run `./gradlew yourBuildVariant`. This will generate an .apk file in the `adblockplussbrowser/app/build/outputs/apk/yourBuildVariant/debug` directory.

#### Downloading Subscriptions
In the `build.gradle` of the `core` module, there is a task to manually download the subscriptions to prepare the release.

- `downloadSubscriptions` is a task that will execute the following:
  - Download Exception Rules (`downloadExceptionRules` gradle task)
    - **_Input_**: flavor
    - **_Output_**: exceptionrules.txt
  - Download Easylist (`downloadEasyList` gradle task)
    - **_Input_**: flavor
    - **_Output_**: easylist.txt
  - Pack Subscriptions with XZ (`packSubscriptionsFiles` gradle task)
    - **_Input_**: flavor
    - **_Uses_**: exceptionrules.txt 
    - **_Output_**: exceptionrules.txt.xz
    - **_Note_**: Only exceptionrules file is being packed. Easylist packing didn't provide any extra benefit.

To run `downloadSubscriptions` a flavor must be provided. Default value is `abp`. E.g.:
- ####ABP
```
    ./gradlew :core:downloadSubscriptions
```
OR
```
    ./gradlew :core:downloadSubscriptions -Pflavor=abp
```

**Output Folder**: `src/main/assets`

- ####Adblock
```
    ./gradlew :core:downloadSubscriptions -Pflavor=adblock
```
**Output Folder**: `src/adblock/assets`
- ####Crystal
```
    ./gradlew :core:downloadSubscriptions -Pflavor=crystal
```
**Output Folder**: `src/crystal/assets`


Importing into Android Studio
-----------------------------

#### Requirements

- [Android SDK][2]
- [Android Studio][3]
- Android Build Tools 30.0.3
- The compilation is done against Android API 30, so make sure you have that one
  installed.
- JDK 8 or above

#### Importing

- Open Android Studio and select *Open an existing Android Studio Project*, then navigate to the `adblockplussbrowser` directory. 
- Choose your build variant.

Application Architecture
------------------------
Adblock Plus for Samsung Internet has been built following the Clean Architecture Principle, [Repository Pattern][7], [MVVM Architecture][8] in the presentation layer as well as in the jetpack components.

#### The app is comprised of the following modules:
- app module contains the `Application` class, the launcher `Activity`, and the main `Activity` with the application navigation graph;
- analytics module contains the functionality of sharing anonymized user tracking;
- base module contains utility and helper classes/functions, base models, and interfaces that are available to all other modules. This is a self-contained module that shouldn't depend on any other;
- i18n module contains translatable string resources;
- onboarding module contains all the Onboarding UI code;
- preferences module contains all the UI code for user preferences. It communicates with the settings module, where the Model lives, and with the core via the `SubscriptionsManager` interface (from the base module);
- settings module contains the Model for the user preferences, the repository, and data sources for user preferences and configurations;
- core module listens to changes in settings and is responsible for download/update of the filters lists accordingly. It is also responsible for scheduling and manages automatic updates and provides a unified filters list file to the Samsung Internet browser. This update mechanism is described below.

API Keys/Tokens
---------------
We read environment variables (production) and local config file (development), then merge them together and pass to build config.

#### Development
When developing locally, it is easier to use a local config file instead of environment variables.
Local config file is `config.local.properties` and should be placed in the root of the ":telemetry" module. It is excluded from git and should not be committed.
Any variable (with any prefix or suffix) from local config file will be added to build config and override the same variable from environment variables.

#### Production (CI)
When building in CI, we read environment variables and pass them to build config.
All environment variables should start with `EYEO_` prefix are added to build config.
For example:
`EYEO_TELEMETRY_ENDPOINT_URL` will be added as `BuildConfig.EYEO_TELEMETRY_ENDPOINT_URL`.

Subscriptions updates
---------------------

#### Manual update/force refresh update now
When the user is using the _Update now_ feature, **all** the _Subscriptions_ are downloaded, even if the user already has a fresh version of any of them.

#### Configurations changes
When the user adds or removes a _Subscription_, adds/removes domains to the allow/block lists, or changes the Acceptable Ads setting, a new `UpdateSubscriptionsWorker` is fired to run immediately.
Configuration changes are debounced by 500ms, so if the user quickly changes more than one setting, they will be combined in just one worker. Otherwise, a new worker will be enqueued for each setting change.

#### Automatic update config changed
After filter lists are requested by the browser, filters are checked to determine if they are expired.
If **Wi-Fi** Only is set, they will be updated if the last update occurred 72 hours or earlier.
If **Always** is set, they will be updated if the last update occurred 24 hours or earlier.

### No configuration changed since the last update
The update is skipped if all of the following criteria are met:
- there are no changes on Active Subscriptions, allow/block lists, and Acceptable Ads status;
- it is not a manual update.

### Adding/removing domains from the allowlist
If the only change is on the allow/block lists, we simply check if the filters file for every active subscription is still present on the filesystem. If a file still exists - it is used, no matter how long ago it was last fetched. If a file is missing - the subscription is downloaded again.

### Adding/removing Subscriptions, changing Acceptable Ads setting
When a new _Subscription_ is added, that file is reused instead of downloading it again if the filter list file already exists and the file was downloaded less than 1 hour ago. If the last successful download was completed more than 1 hour ago, then the _Subscription_ is downloaded again (respecting `If-Modified-Since` and `If-None-Match` headers).
Other active _Subscriptions_ are checked only for the existence of the filter list file. The same applies to _Acceptable Ads Subscription_, which is treated like any other _Subscription_ internally.

### Failures
If a _Subscription_ fails to download, a previously downloaded version will be used if available. Otherwise, the resulting filters list file will be created without this _Subscription_.
When a worker fails to update a _Subscription_ it is marked on a `Retry` state and will be retried 4 times with an exponential backoff strategy.

Testing
-------
To test the whole functionality of the application you will need an Android device with Android 5.0+ (Lollipop) and Samsung Internet 4.0+.

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
[9]: https://www.android.com/versions/lollipop-5-0/
