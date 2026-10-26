# OMLib (1.20.1+)

The library of the Open Modular mods (OpenModularTurrets, OpenModularLighting and later ones) for Minecraft 1.20.1 and newer.
It is a mod of its own (mod id `omlib`), built for the same six nodes as the mods (1.20.1 Fabric and Forge, 1.21.1 Fabric and NeoForge, 1.21.11 Fabric and NeoForge) with the same Stonecutter setup.
Every mod mounts this repository as its `omlib` git submodule and bundles the library into its own jar (jar-in-jar), so players install one file per mod.
This branch starts from a clean history; the Minecraft 1.12 library lives on the `1.12` branch.

## What is in it

- `src/main/java/omtteam/omlib/` and `src/client/java/omtteam/omlib/client/` are the library code, in the same package layout the mods had before:
  - `platform` is the loader abstraction (`Platform`, `LoaderInfo`, `Fuel`, `Network`, `Menus`, `Energy`) with the Fabric, NeoForge and Forge implementations in `platform/<loader>`.
    The services are created on first use, so it does not matter which mod starts first.
  - `compat` hides the differences between Minecraft versions (`VersionCompat`, `NbtCompat`, `BlockCompat`, `NetworkCompat`, `MenuCompat`, `CompatBlockEntity`, `CompatEntityBlock` with `BlockCodec`, `CommonTags`); `client.compat` has the client side (`ClientCompat`).
  - `block.InteractiveBlock` is the right-click handling for all mods: blocks implement `onUse`, and mods add `Interceptor`s that see every click first (a wand, a settings card); the library registers the loader's event once.
  - `core.energy` (`EnergyStore`, `EnergyAccess`, `EnergyGroup`) and `inventory.ItemAccess` describe energy and items other blocks may use, and `blockentity.TransferHost` marks the block entities that expose them; the loaders' lookups and capabilities are registered from the mod's list of such block entity types.
  - `config` is the annotation-based JSON config (`@Comment`, `@Range`, `ConfigWriter`, `ConfigRanges`, `ConfigValidator`, `ConfigMigration`) and `ConfigManager<T>`, which loads, migrates, validates and writes a mod's file and holds the server's config on clients.
    The library sends every manager's config to joining players in one packet (`omlib:config_sync`, routed by mod id) and resets them when the player leaves; a mod keeps a small facade class (`OMTConfig`, `OMLConfig`) around its manager.
  - `camo`, `menu`, `network` and `client` hold the camouflage menu: `CamoHost`, `CamoBlock`, `CamoData`, `DisguiseRules`, the menu, its packets and screen, and the models that draw a disguise.
    The library registers the menu type and the packets (namespace `omlib`) itself; a mod provides its rules and hosts.
- `src/main/resources/assets/omlib/lang/` has the texts of the library's screens and messages.
- `build-logic/` is a Gradle included build with the shared build logic of this repository and of the mods: the `mod-common` convention plugin and `convertFabricData`.
- `tools/shared_sources.py` and `shared-sources.txt` keep the few source files that the mods still copy from each other identical (see below).
- `tools/bootstrap.sh` builds the library for all nodes into `build/maven`; the mods call it from their `settings.gradle.kts`.

## Using it in a mod

The mod mounts this repository as a git submodule:

```bash
git submodule add -b 1.20.1+ ../OMLib.git omlib
```

and its `settings.gradle.kts` builds the library first and includes its build logic:

```kotlin
pluginManagement {
    val omlibDir = File(settingsDir, "omlib")
    if (File(omlibDir, "tools/bootstrap.sh").exists() && System.getenv("OMLIB_SKIP_BUILD") == null) {
        val build = ProcessBuilder("sh", "tools/bootstrap.sh").directory(omlibDir).inheritIO()
        build.environment()["JAVA_HOME"] = System.getProperty("java.home")
        check(build.start().waitFor() == 0) { "Building the omlib submodule failed (see above)" }
        // drop Loom's remapped copy of the old library (see the settings.gradle.kts of the mods)
    }
    includeBuild("omlib/build-logic")
    // repositories …
}
```

The build scripts then get the library's coordinates from `mod-common` (`project.extra["omlibCoordinates"]`, `["omlibVersion"]`) and declare it:

- Fabric: `modImplementation(omlib)` and `"include"(omlib) { isTransitive = false }`.
- NeoForge: `implementation(omlib)` plus `jarJar("omtteam.omlib:omlib")` with the version range `[<omlib version>,)`; the client and game test source sets get it as `clientCompileOnly` / `gametestCompileOnly`.
- Forge 1.20.1: the same with `modImplementation` (the jar is SRG-named and gets remapped).

The mod's metadata declares the dependency (`omlib` in `fabric.mod.json`, `[[dependencies.<mod>]]` with `ordering = "AFTER"` on NeoForge and Forge); `${omlib_version}` is filled in by `mod-common`.

What the mod provides to the library:

- **Entrypoints:** the mod no longer installs the platform services, the right-click event, the join and disconnect events or the config sync packet; it calls `FabricBlockLookups.register(ModBlockEntities.transferTypes())` (Fabric), `NeoForgeCapabilities.register(event, types)` in its own `RegisterCapabilitiesEvent` (NeoForge) and, on NeoForge, applies its packets in its own `RegisterPayloadHandlersEvent` with `((NeoForgeNetwork) Platform.network()).applyRegistrations(event, MOD_ID)`.
  Forge attaches the capabilities to every `TransferHost` from the library's own entrypoint.
- **Camouflage:** a block that can be disguised implements `CamoBlock` (with `overlayTintIndex()` if part of its own model stays visible on top); its block entity implements `CamoHost` and returns its `DisguiseRules`.
  The library registers the menu, packets, screen and model wrapping once for all mods.
- **Config:** a mod's config object is a plain class with `@Comment` / `@Range` fields; its sections are the classes in the same package.
  The mod creates one `ConfigManager` (`new ConfigManager<>(MOD_ID, ConfigData.class, ConfigData::new, ConfigData::validate, steps)`) and calls `load()` at start-up.
- **Interactions:** `InteractiveBlock.addInterceptor(...)` at start-up, for things that act on any block.

## Building

```bash
./gradlew chiseledBuild          # builds, tests and verifies all six nodes
./gradlew chiseledPublishLocal   # puts the jar of every node into build/maven (what the mods' bootstrap does)
./gradlew "Set active project to 1.20.1-fabric"   # Stonecutter: switch the active node (switch back to 1.21.1-fabric before committing)
```

`chiseledPublish` uploads every node to CurseForge / Modrinth when `curseforge_project_id` / `modrinth_project_id` are set; the mods bundle the library, so this is optional.
The nodes publish into one Maven repository, so the build runs without Gradle's parallel mode.
The mods rebuild the library when its sources change (the bootstrap compares a hash of them); `OMLIB_SKIP_BUILD=1` skips that.

## Changing the library

1. Edit the files in `omlib/` of any mod (it is a normal git checkout of this repository) or in a checkout of this repository.
2. Commit and push in the library.
3. In each mod, commit the new submodule pointer (`git add omlib`).

A mod's build only changes when its pointer moves, so the mods can take an update one at a time.
Cloning a mod needs the submodule: `git clone --recurse-submodules <mod url>` or, in an existing clone, `git submodule update --init`.

## Shared sources

A few files are still copies in the mods (the game test stand-ins for Minecraft 1.21.11, the PMD ruleset, the Gradle wrapper properties).
They differ only by the mod's package, id, name and class prefix, which the tool normalises before comparing; `shared-sources.txt` lists them.

```bash
# from a mod root, with the other mods as siblings
python3 -I omlib/tools/shared_sources.py check . ../OpenModularLighting
python3 -I omlib/tools/shared_sources.py sync --from . . ../OpenModularLighting     # dry run, add --write to copy
```

## License

The source code is licensed under the [MIT license](LICENSE), and art under the [CC-BY-NC-SA 4.0 license](LICENSE-ART) (this repository contains none so far; [LICENSE-DUAL](LICENSE-DUAL) states both, like in the mods).
Contributions are covered by the [CLA](CLA.md).
