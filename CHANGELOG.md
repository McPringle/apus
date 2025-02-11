# Changelog

Please report any bugs and feature requests via our
[GitHub Issue Tracker](https://github.com/McPringle/apus/issues).
For questions and support requests, please use
[GitHub Discussions](https://github.com/McPringle/apus/discussions).

---

## Version 5

**Release date: work in progress 🚧**

### New Features

* Add support for BlueSky
* Add support for Devoxx events (including Voxxed Days)
* Configure UI language via environment variable
* Dynamically generate social headline based on enabled plugins
* Hashtags are now handled independent of the social media plugins.

### Fixed Bugs

* Fix internationalization / localization issues

### Maintenance Work

* Add SBOM (Software Bill of Materials) generation
* Shorten README for DockerHub
* Prefix all environment variables with `APUS`

---

## Version 4

**Release date: 2024-10-28**

### New Features

* Add configuration option to adjust event dates
* Add configuration option to hide closed event rooms
* Display optional image below the event agenda
* Add support for configuring multiple hashtags
* Add support for speaker pictures (overrides track icon)
* Add social demo plugin for demo and testing purposes
* Release docker images for ARM processors

### Fixed Bugs

* Fix agenda column width calculation
* Fix session view with very long titles
* Fix hide empty room setting

### Maintenance Work

* Upgrade dependencies
* Fix linter warnings
* Add test coverage monitoring

---

## Version 3

**Release date: 2024-09-15**

### New Features

* Support full-screen social media post view without conference agenda
* Adjusting number of agenda columns automatically as needed
* Overwrite automatically generated social media headline

### Fixed Bugs

* Fixed UI quirks

### Maintenance Work

* Update all dependencies
* Fixed a bunch of linter warnings

---

## Version 2

**Release date: 2024-08-12**

### Features

* Add support for event agenda hosted on Sessionize.com (BaselOne, Java Forum Nord, etc.)
* Add support for Java Forum Stuttgart
* New demo event plugin to show fake data for demos and testing
* The update frequency of the event agenda is now configurable
* Add support for session tracks with default icons and custom SVG images
* Theming: all colors are now configurable
* Create hashed passwords for admin access on the command line
* New config option to show/hide event room legend
* Time limit for next session is now configurable

### Bugfixes

* Fixed possible XSS attack with modified Mastodon posts
* Fixed incorrect calculation of minutes remaining 

### Maintenance

* All icons are now system independent
* Sessions without a language will not display a language flag
* Better use of available space for social media posts
* Refactored plugin interface to use streams instead of lists

---

## Version 1

**Release date: 2024-04-10**

### Features

* Display agenda of DOAG events (JavaLand, CyberLand, CloudLand, etc.)
* Display posts of Mastodon containing a configurable hashtag
* Display images from Mastodon posts (configurable)
* Filter sensitive Mastodon posts
* Filter Mastodon replies
* Filter Mastodon posts by words
* Hide single Mastodon posts
* Block Mastodon profiles
* English and German translation
* Dynamic updates of Agenda and Mastodon posts
