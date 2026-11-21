# Changelog

All notable changes to OMLib.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- NEEPMeat (Fabric 1.20.1): its vascular network charges the energy of the mods' blocks (1 eJ = 1 E); a block takes only what its energy still accepts
- Containers whose slots hold more than one stack (`LargeStackContainer`, with menu slots, menu and screen): saved, synced and moved by hoppers and pipes on all loaders; counts from four digits on are drawn smaller, the tooltip shows count and limit
- Core API from OpenModularCore: `MachineMode` (with `byOrdinal`) and `TargetingSettings` (`core.machine`), `CamoPermissions` (`core.camo`) and the memory card's `MemoryCardData` and trust copy rules `MemoryCardRules` (`core.card`), formerly in OpenModularTurrets; `SecurityEnvironment.isSharedOwner` and `offlineModeSupport` (default `false`) and `TrustManager.getTrustedPlayer(PlayerIdentity)`

### Changed

- The core classes (`omtteam.omlib.core`) come from the shared OpenModularCore 2.1.0 (module `omlib-core`, the same on every Minecraft version) and are bundled into the jar; `PlayerInfo` refuses a null UUID or name

### Fixed

- Forge 1.20.1: the common item tags (`c:iron_ingots`, `c:gold_ingots`, `c:redstone_dusts`, `c:quartz`, `c:diamonds`, `c:glass_panes`) are defined from Forge's own tags; Forge has none, so recipes using them matched no items
