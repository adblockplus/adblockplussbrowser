# Changelog
All notable changes to Adblock Plus for Samsung Internet will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2021-11-25
### Changed
- About page links look like links: underlined and the color adjusted
- Reduced the app size

### Added
- Tranlsations to 15 languages
- Process the UTM links coming from Google Play Store
- Card about browsing in multiple languages on settings screen
- `detect`: stats code analyzer

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
