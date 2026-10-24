# Changelog

All notable changes to OMLib for Minecraft 1.12.2.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Add every user-visible change as a one-line entry under **Unreleased** (sections: Added, Changed, Fixed, Removed), by hand or with `./gradlew changelogEntry -Ptype=added -Pentry="…"`.
Before a release, `./gradlew releaseChangelog` turns *Unreleased* into the section of the version set in `gradle.properties`; the CurseForge and Modrinth uploads of that version (`./gradlew release` in the mod folder) use exactly that section.

## [Unreleased]

## [4.0.0-beta.1] - 2026-10-24

### Changed

- For addon authors: `ICamoSupport.getOwnedBlock()` is now `getCamoTileEntity()` and returns any `TileEntity`, so camouflage hosts no longer need to be owned blocks

## [4.0.0-alpha.2] - 2026-10-23

### Changed

- License: the source code is now MIT and the art CC-BY-NC-SA 4.0 (see `LICENSE`, `LICENSE-ART`, `LICENSE-DUAL`)

### Fixed

- Machines answered other mods' Redstone Flux (RF) capability requests with nothing, and crashed with Redstone Flux installed but not Tesla
- Camouflaged blocks could render as grass while their tile entity was missing
- The trusted players GUI crashed on the remove and permission buttons when no player was selected
- The target settings GUI crashed when closed for machines without client sync

### Removed

- The IC2 config options `EUSupport` and `EUtoRFRatio` (they had no effect since IC2 support was removed)
- For addon authors: the unused device network API (`api.network.OMLibNetwork`, `INetworkTile`, `IPowerExchangeTile`, `IController`, `INameController`) and helpers (`Tuple3`, `EntityUtil`, `ItemUtil`, `ItemStackList`, `PropertyTools`, `BlockingAbstractGui`)

## [4.0.0-alpha.1] - 2026-10-23

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
