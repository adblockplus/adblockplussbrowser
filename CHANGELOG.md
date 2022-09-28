# Changelog
All notable changes to Adblock Plus for Samsung Internet will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.4.0] - 2022-09-28
### Fixed
- Crashes in MainPreferencesFragment

### Changed
- Redundant HEAD requests are not sent
- Analytics events download_http_error are reported via crashlytics error instead of firebase property
- Analytics events user_counting_http_error are reported via crashlytics error instead of firebase property

### Added
- Load custom filter lists from local storage
- Test pages filter lists are added to debug ABP version by default
- Target SDK version is set to Android 12
- JaCoCo combined coverage
- Unit tests of FilterListContentProvider.kt
- Enhance the test coverage of OkHttpDownloader.kt
- Enhance the test coverage of UserCounterWorker.kt

## [2.3.0] - 2022-04-06
### Fixed
- Non-translated elements on "More blocking options" screen are not changing in case of a language change event
- Enable SI has gigantic graphic
- Default filters files are not customized

### Changed
- Removed analytics events from Privacy Policy and Terms of Use
- Hidden hint when custom subscriptions and allowlist are empty
- Allowed HTTP subscriptions
- Manual updates are disabled if an update is already happening
- Closing AA dialog button
- Redundant HEAD requests are not sent
- Removed progress dialog
- Progress style "Update Now" changed to linear
- Use the latest filters files in case of download error
- Progress indicator should not move the UI when it appears and disappears

### Added
- Gradle Task Download Assets
- Subscriptions Worker Test
- Running all the unit tests on CI
- Alternative packing method for preloaded AA subscriptions

## [2.2.0] - 2022-02-22
### Fixed
- Hyperlinks for Terms & Privacy Policy are invisible in Arabic
- Missing lastVersion parameter for download filters and user counting requests
- The "Download Samsung Internet" crash for devices without stores and a default browser
- Non-translated elements on "More blocking options" screen
- About Privacy and Terms Rows click listener

### Changed
- Updated preloaded filter list
- The "Download Samsung Internet" dialogue can be canceled
- Users for which ABP for SI is already activated in the SI browser are not redirected to the SI settings
- Added accessibility content descriptions for the UI.
- Added explanatory hint for removing custom list or allowlist entry
- Rename ABP4SI to fit into 30 chars (new Google Play metadata policy)
- Minimalize number for failed user counting requests through work request API
- "Browsing in multiple languages?" is not shown and when the user has completed onboarding
- Exceptionrules download links

## [2.1.1] - 2021-12-28
### Changed
- Updated preloaded filter list

### Fixed
- Adblock analytics user counting property

## [2.0.1] - 2021-12-07
### Changed
- Removed unused resources

### Fixed
- Crash in data migration from the old app

## [2.0.0] - 2021-11-25
### Changed
- Links on the "About page" are better formatted
- Reduced the app size

### Added
- Translations to 15 languages
- Process the UTM links coming from Google Play Store
- Card about browsing in multiple languages on the settings screen

### Removed
- Redundant log output
- Privacy screen on onboarding
- Improved privacy: not collecting Advertising ID and Android ID for Google Analytics anymore

### Fixed
- Code cleanup
- Small bugfixes and improvements

## [2.0.0-rc1] - 2021-10-01 - This release contains all current commits from https://gitlab.com/eyeo/distpartners/adblockplussbrowser/-/tree/dev

### Changed/Added
- Fully rebuilt user interface
- Added dark and light modes support
- Added onboarding module for guiding the user through the installation process
- Added portrait and landscape modes
- Improved start up performance
- Stability improvements and bug fixes
- Added mechanism to migrate all data from version 1.2.1 to 2.0.0-rc1
- Added analytics module for sharing anonymized user behavior

### Known issues
- "Open Samsung Internet" button on the "Enable Adblock Plus" sometimes causes Samsung Internet to show an empty ad blockers list
