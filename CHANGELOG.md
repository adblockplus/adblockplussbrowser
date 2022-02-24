# Changelog
All notable changes to Adblock Plus for Samsung Internet will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [crystal-2.2.0] - 2022-02-24
### Fixed
- Hyperlinks for Terms & Privacy Policy are invisible in Arabic
- Missing lastVersion parameter for download filters and user counting requests
- The "Download Samsung Internet" crash for devices without stores and a default browser
- Non-translated elements on "More blocking options" screen
- About Privacy and Terms Rows click listener
- Crash on a SecurityException from LauncherActivity
- Accept and domain-parse complete URLs
- Allowlisting filter domain restriction
- Mechanism for counting Yandex users
- Crash in data migration from the old app
- Crash on a SecurityException from LauncherActivity

### Changed
- UI customizations for Crystal
- Updated preloaded filter list
- The "Download Samsung Internet" dialogue can be canceled
- Users for which ABP for SI is already activated in the SI browser are not redirected to the SI settings
- Added accessibility content descriptions for the UI.
- Added explanatory hint for removing custom list or allowlist entry
- Minimalize number for failed user counting requests through work request API
- "Browsing in multiple languages?" is not shown and when the user has completed onboarding
- Exceptionrules download links
- Removed unused resources
- Updated preloaded filter list
- Improved error reporting
- Links on the "About page" are better formatted
- Reduced the app size
- Improved start up performance

### Added
- Translations to 15 languages
- Process the UTM links coming from Google Play Store
- Card about browsing in multiple languages on the settings screen
- Fully rebuilt user interface
- Added dark and light modes support
- Added onboarding module for guiding the user through the installation process
- Added portrait and landscape modes
- Added mechanism to migrate all data from version 1.2.2 to 2.2.0
- Added analytics module for sharing anonymized user behavior

## [2.1.1] - 2021-12-28
### Changed
- Updated preloaded filter list

## [3.1.1] - 2021-12-27
### Fixed
- Crash on a SecurityException from LauncherActivity

## [3.1.0] - 2021-12-20
### Changed
- AdBlock specific filter list

### Fixed
- Adblock analytics user counting property

## [3.1.0-rc3] - 2021-12-17

### Fixed
- Adblock flavor name assets

## [3.1.0-rc2] - 2021-12-16

### Fixed
- Adblock flavor name

## [3.1.0-rc1] - 2021-12-16
### Changed
- Randomized subscription url
- Updated Android Gradle Plugin and Gradle
- Apply Adblock color rebranding
- Improved error reporting
- Track Adblock for SI separately from Adblock Plus for SI
- Replace Adblock Plus and eyeo in Copyright notice for Adblock

### Fixed
- Accept and domain-parse complete URLs
- Allowlisting filter domain restriction
- Mechanism for counting Yandex users

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
- "Enable Adblock Plus" is shown even if an existing user has enabled it in the previous ABP4SI version.
