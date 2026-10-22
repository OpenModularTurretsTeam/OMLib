# Changelog

All notable changes to OMLib for Minecraft 1.12.2.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Add every user-visible change as a one-line entry under **Unreleased** (sections: Added, Changed, Fixed, Removed), by hand or with `./gradlew changelogEntry -Ptype=added -Pentry="…"`.
Before a release, `./gradlew releaseChangelog -Pchangelog_version=<tag without v>` turns *Unreleased* into the release section; the CurseForge upload of that tag uses exactly that section.

## [Unreleased]

### Changed

- Members of the owner's scoreboard team count as trusted (lowest access level) for owned blocks
- Stored energy above a lowered capacity is cut off

### Fixed

- Very old trust lists (saved as a plain list of names) are read again

### Removed

- Global trust lists: they could not be turned on in game
- The unfinished network cable block (it crashed the client with missing model variants); placed cables disappear

## [3.2.0]

### Changed

- First work towards GUI for target players

### Fixed

- Fix trusted players system
- Fix share owner not saving
- Fix share owner del command not working
