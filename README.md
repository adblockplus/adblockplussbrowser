Adblock Plus for Samsung Internet
=================================

A content blocker application that serves our filter lists to Samsung's
content blocker enabled internet browser.


Building with Gradle
--------------------

#### Requirements

- [Android SDK][1]
- Android Build Tools 25.0.3 *
- JDK 7 or above

\* Edit `buildToolsVersion` in the root `build.gradle` to change the Build Tools version, if necessary.

#### Building

- In the root dir, create a `local.properties` file and set the `sdk.dir=/your/path/here`
- From the root dir, run `./gradlew assembleDebug`. This will generate an .apk file in the `adblockplussbrowser/adblockplussbrowser/build/outputs/apk` dir.

Importing into Android Studio
-----------------------------

#### Requirements

- [Android SDK][1]
- [Android Studio][2]
- Android Build Tools 25.0.3 *
- We're compiling against Android API 25, so make sure you have that one
  installed.
- JDK 7 or above

\* Edit `buildToolsVersion` in the root `build.gradle` to change the Build Tools version, if necessary.

#### Importing

Open Android Studio and select *Open an existing Android Studio Project*, then navigate to the `adblockplussbrowser` dir.

Importing into Eclipse
----------------------

#### Requirements

- [Android SDK][1]
- [Eclipse][3]
- ADT (Android Developer Tools), available via *Eclipse Marketplace*
- We're compiling against Android API 25, so make sure you have that API
  installed.
- JDK 7 or above

#### Importing

Choose *File*->*New*->*Other*->*Android*->*Android Project from Existing Code*,
then navigate to the `adblockplussbrowser/adblockplussbrowser` dir.


Testing
-------

To test the whole functionality of the application you will need a Samsung
Android device with Android 5.0+ (Lollipop) and Samsung Internet 4.0+.

Emulators won't help you here, unfortunately.


[1]: http://developer.android.com/sdk/
[2]: https://developer.android.com/studio/index.html
[3]: https://eclipse.org/
