# Changelog

Please report any bugs and feature requests via our
[GitHub Issue Tracker](https://github.com/McPringle/apus/issues).
For questions and support requests, please use
[GitHub Discussions](https://github.com/McPringle/apus/discussions).

---

## Version 4

**Release date: work in progress 🚧**

### New Features

### Fixed Bugs

* Fix agenda column width calculation

### Maintenance Work

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
