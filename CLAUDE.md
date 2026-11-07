# CLAUDE.md — OMLib (1.20.1+)

The library of the Open Modular mods (OpenModularTurrets `../OpenModularTurrets`, OpenModularLighting `../OpenModularLighting`), a mod of its own (`omlib`) that the mods bundle jar-in-jar.
Read `README.md` for the layout and how the mods use it.
It is built like the mods: Stonecutter nodes `<minecraft version>-<loader>` (1.20.1 Fabric/Forge, 1.21.1 Fabric/NeoForge, 1.21.11 Fabric/NeoForge), Mojang mappings, the `mod-common` convention plugin from `build-logic/`.
The same rules apply as in the mods' `CLAUDE.md`: committed source in the `1.21.1-fabric` state, loader-specific classes in `platform/<loader>` packages (no loader conditions in gameplay-agnostic code), no new dependency without asking, Markdown with one sentence per line, no Fabric API or Team Reborn Energy outside `platform/fabric`.

## Rules for library code

- The library knows no mod: no mod id, no mod class, no mod-specific text.
  Everything mod-specific comes in through an interface the mod implements (`DisguiseRules`, `CamoHost`, `TransferHost`, the lists a mod passes to the registrars) or a parameter.
- Its own ids use the namespace `omlib` (`OMLib.id`): the camo menu type, the camo packets, its translation keys (`gui.omlib.*`, `message.omlib.*`, `tooltip.omlib.*`).
  A mod's texts that the library shows come from the mod's `DisguiseRules` (`invalidMessage`, `slotHint`).
- Public API stays small: classes and methods are public only if a mod uses them.
  A change of a public signature changes both mods; check them (`./gradlew chiseledBuild` in each) before committing.
- `core/` stays free of Minecraft (`verifyCoreIsolation`).
- The platform services are created on first use (`Platform`, `ClientPlatform`), never relying on which mod's entrypoint runs first.
- NeoForge payloads: a mod applies the registrations of its own namespace in its own `RegisterPayloadHandlersEvent` (`NeoForgeNetwork.applyRegistrations`).
- Reflection on config objects (`ConfigWriter`, `ConfigRanges`) finds the sections in the package of the config root's class.
- A `ConfigManager` must not validate in its constructor (a mod's defaults may depend on state set up after it); `load()` validates.
- The NBT keys of the security classes (`Owner`, `TrustedPlayers`, `UUID`, `Name`, `AccessLevel`) and the ids of the wand's data (`OwnershipWandNoSpread`) are part of the mods' saves: do not change them.
- `OMLibCount`, the key of a count above one stack in the item lists of `VersionCompat.saveItems`, is part of the mods' saves too (on 1.20.1 it also carries the count of a synced stack, `LargeStackMenu`).
  Vanilla saves such a stack with a count of 1, so a save read without the key loses the extra items but stays loadable.
- Ownership rules that a mod decides (config values) come through `OwnershipSettings`; never read a mod's config from the library.
- `InteractiveBlock` interceptors run in registration order (the library's own, the ownership wand and claiming, come first), first non-PASS wins; they are for behaviour that applies to any block, not for a mod's own blocks (those implement `onUse`).

## Working with the mods

The mods' `omlib/` folders are clones of this repository.
The mods' settings script builds the library into `omlib/build/maven` whenever its sources changed (`tools/bootstrap.sh`, a hash of the sources) and drops Loom's cached remapped copy of the old build.
Gradle must not run in parallel here (the nodes write one `maven-metadata.xml`).
Never edit inside a mod's `omlib/`: work in this repository (the workspace's `OMLib/`), where changes are not lost on the next submodule checkout and reach both mods.
After changing the library, build it here (`./gradlew chiseledBuild`) and commit it, then move the submodule in each mod straight from this clone, without a push (`git -C OpenModularTurrets/omlib fetch ../../OMLib 1.20.1+ && git -C OpenModularTurrets/omlib checkout FETCH_HEAD` from the workspace root, the same for `OpenModularLighting`), build both mods and commit the pointers (`git add omlib`) when the user asks.
The library commit must be pushed before a mod that points to it (the workspace's `push.sh` checks that).
The few files the mods still copy from each other are listed in `shared-sources.txt` (check and sync with `tools/shared_sources.py`, see `README.md`).
