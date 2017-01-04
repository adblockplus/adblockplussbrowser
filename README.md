Adblock Plus for Samsung Internet
=================================

A content blocker application that serves our filter lists to Samsung's
content blocker enabled internet browser.


Building with Ant
-----------------

#### Requirements

- [Android SDK][1]
- [Ant][2]

Just type `ant` inside the `adblockplussbrowser` folder to get a list of
available build options.


Importing into Eclipse
----------------------

#### Requirements

- [Android SDK][1]
- [Eclipse][3]
- ADT (Android Developer Tools), available via *Eclipse Marketplace*

We're compiling against Android API 23, so make sure you have that one
installed.

#### Importing

Choose *File*->*New*->*Other*->*Android*->*Android Project from Existing Code*,
then navigate to the `adblockplussbrowser` folder.


Testing
-------

To test the whole functionality of the application you will need a Samsung
Android device with the latest official Android Marshmallow release.

Emulators won't help you here unfortunately.


[1]: http://developer.android.com/sdk/
[2]: https://ant.apache.org/
[3]: https://eclipse.org/
