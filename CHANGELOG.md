# Changelog

All notable changes to OMLib.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Glow effects for renderers: `GlowGeometry` (spheres and double-sided quads), `OrbitTrailRenderer` (a sphere with a fading trail circling on a `WavyRing`), taken over from OpenModularTurrets
- Containers whose slots hold more than one stack (`LargeStackContainer`, with menu slots, menu and screen): saved, synced and moved by hoppers and pipes on all loaders; counts from four digits on are drawn smaller, the tooltip shows count and limit
- Core API from OpenModularCore: `MachineMode` (with `byOrdinal`) and `TargetingSettings` (`core.machine`), `CamoPermissions` (`core.camo`) and the memory card's `MemoryCardData` and trust copy rules `MemoryCardRules` (`core.card`), formerly in OpenModularTurrets; `SecurityEnvironment.isSharedOwner` and `offlineModeSupport` (default `false`) and `TrustManager.getTrustedPlayer(PlayerIdentity)`

### Changed

- The core classes (`omtteam.omlib.core`) come from the shared OpenModularCore 2.3.0 (module `omlib-core`, the same on every Minecraft version) and are bundled into the jar; `PlayerInfo` refuses a null UUID or name
