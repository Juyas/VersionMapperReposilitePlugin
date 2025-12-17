# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - ${maven.build.timestamp}

### Added
- Initial release
- REST API with two endpoints: `api/pommapper/id/{id}` and `api/pommapper/repo/{repository}/{gav}`
- REST API query parameter `snapshots` to enable/disable snapshots; defaults to `true`
- REST API query parameter `releases` to enable/disable releases; defaults to `true`
- REST API query parameter `limit` to limit the number of results; defaults to `-1` representing no limit
- REST API query parameter `since` to filter results by group version; defaults to `0.0.1`
- REST API may be configured via the shared reposilite settings
- Settings that offer the ability to define artifacts to be mapped and their respective repositories
- Settings that offer the ability to define xPaths for pom.xml readings to be included in the JSON response of the REST API
- Cache to speed up REST API responses
- Automatic cache update on deploy
- Command to update the cache via the console
- Validation of the plugin settings via the console

### Changed

### Deprecated

### Removed

### Fixed

### Security
