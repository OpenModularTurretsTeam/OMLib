# Changelog

All notable changes to OMLib for Minecraft 1.12.2.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Add every user-visible change as a one-line entry under **Unreleased** (sections: Added, Changed, Fixed, Removed), by hand or with `./gradlew changelogEntry -Ptype=added -Pentry="…"`.
Before a release, `./gradlew releaseChangelog` turns *Unreleased* into the section of the version set in `gradle.properties`; the CurseForge and Modrinth uploads of that version (`./gradlew -b build.gradle release`) use exactly that section.

## [Unreleased]

### Changed

- Members of the owner's scoreboard team count as trusted (lowest access level) for owned blocks
- Stored energy above a lowered capacity is cut off
- For addon authors: the client-only GUI and render helpers moved from `omtteam.omlib.api.gui` / `api.render` to `omtteam.omlib.client.gui` / `client.render` (`ColorOM`, `ICamoSupport` and `RenderBlockStateContainer` stay)
- For addon authors: `ICamoSupport` has four new methods (`isCamoEnabled`, `supportsLight`, `canSetCamo`, `canChangeLight`)

### Fixed

- Very old trust lists (saved as a plain list of names) are read again

### Removed

- Global trust lists: they could not be turned on in game
- The unfinished network cable block (it crashed the client with missing model variants); placed cables disappear
- Setting camouflage by right-clicking with a block (and resetting it by sneaking empty-handed); the camouflage menu does it now

### Added

- Camouflage menu for camouflaged blocks: a disguise slot (click with a block to set it, empty-handed to reset) and, where supported, light value and opacity sliders## [3.2.0]
- Memory card rules (`core.card`: card data, who may copy and write, trust list copying) shared by turret bases and camouflage traps

### Changed

- First work towards GUI for target players

### Fixed

- Fix trusted players system
- Fix share owner not saving
- Fix share owner del command not working
