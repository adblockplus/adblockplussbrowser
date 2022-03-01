# Changelog
All notable changes to Crystal Adblock for Samsung will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.2.0] - 2022-02-24
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

### Known issues
- "Open Samsung Internet" button on the "Enable Adblock Plus" sometimes causes Samsung Internet to show an empty ad blockers list
