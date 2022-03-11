# Changelog
All notable changes to AdBlock for Samsung Internet will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.2.0] - 2022-02-23
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
- Minimalize number for failed user counting requests through work request API
- "Browsing in multiple languages?" is not shown and when the user has completed onboarding
- Exceptionrules download links

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

### Known issues
- "Open Samsung Internet" button on the "Enable Adblock Plus" sometimes causes Samsung Internet to show an empty ad blockers list