# Changelog

All notable changes to OMLib for Minecraft 1.12.2.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Add every user-visible change as a one-line entry under **Unreleased** (sections: Added, Changed, Fixed, Removed), by hand or with `./gradlew changelogEntry -Ptype=added -Pentry="…"`.
Before a release, `./gradlew releaseChangelog` turns *Unreleased* into the section of the version set in `gradle.properties`; the CurseForge and Modrinth uploads of that version (`./gradlew release` in the mod folder) use exactly that section.

## [Unreleased]

### Added

- For mod developers: tile entity base `tileentity.TileEntitySynced` (data hooks `readData` / `writeData`, separate client data `writeClientData`, `syncToClient`, `remesh`; from OpenModularLighting); `TileEntityBase` now builds on it and behaves as before
- For mod developers: `container.WindowValues` syncs int values of a container to its GUI as window properties (two 16-bit halves each)
- The server sends its OMLib config values (`omlib.cfg`, e.g. OP access and offline mode support) to clients when they join, so GUIs show the server's rules; after leaving, the client uses its own config again

### Changed

- The jar files are named `omlib-<version>-mc1.12.2-forge.jar` (Minecraft version with `mc`, as on 1.20.1+)

### Removed

- For mod developers: the tile entity base classes `TileEntityElectric`, `TileEntityContainerElectric`, `TileEntityMachine`, `TileEntityTrustedMachine` and `TileEntityContainer`; tile entities hold the helpers `power.EnergyCapabilities` and `tileentity.InventoryNbt` instead (same NBT keys)

### Fixed

- Loading a world kept the owner share lists (`/omlib share`) of the world opened before it in the same game
- A renamed player (or, in offline mode, a player with another UUID) could be missed in lookups of players and trusted players: their hash ignored how they are compared
- Clients could make the server load or generate chunks anywhere by sending trust list, camouflage or GUI messages for far-away positions; these messages now need the block loaded and the player within reach
- Players without access to a block could receive its updates (trust list, settings) by sending its GUI-open message
- Changes in OMLib's config GUI were only applied after a restart
- The debug tool crashed a dedicated server when used on a turret base
- Items dropped from a broken machine lost data other mods keep on the item stack (capabilities)
- Camouflaged blocks could create their tile entity on the chunk render thread (block model, block colour), which can crash or corrupt the world; for mod developers: `WorldUtil.getTileEntity(IBlockAccess, BlockPos)` looks up a tile entity without creating it

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
