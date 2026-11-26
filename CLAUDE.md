# OMLib 1.12.2: the shared core in the builds, security and energy decisions

Moved out of the workspace's top-level `CLAUDE.md` (`../CLAUDE.md` from here), so that it is loaded only when you work in `OMLib/` (it also covers how every 1.12 mod wires in OpenModularCore); the top-level file has the index.
Section names in quotes ("The shared core", "Running", "Versions", …) and relative paths (`../OpenModularCore`, `../CLAUDE.md` is the OMM root file) are those of the top-level file; paths are relative to the workspace folder `OMTTeam1.12/`.

## The shared core (OpenModularCore, since 2026-11-19)

The game rules (`omtteam.omlib.core`, `omtteam.openmodularturrets.core`, `omtteam.openmodularlighting.core`) are no longer in the repos: they come from OpenModularCore (`../OpenModularCore`, see `../CLAUDE.md`, "Shared core logic"), the same source as on 1.20.1, 1.21.1 and 1.21.11.
- **Modules:** 1.12 uses the Java 8 modules, downgraded with JvmDowngrader: OMLib `omlib-core-j8` (`api`, so OMT, OMPD, OML and `runenv` get it through `:OMLib`), OMT `omt-core-j8`, OML `oml-core-j8`; `core_version` in the mod's `gradle.properties`.
  OMPD has no core module of its own (its rules are game layer: `config.DefenseTier`, `config.DefenseStats`, `util.FenceRules`); it gets OMLib's core through OMLib.
  Its `gradle.properties` still names `core_version` (its build does not read it), so `../check-core-version.sh` holds it to the same version as the others (decided by the user, 2026-11-19).
- **JitPack:** `com.github.OpenModularTurretsTeam.OpenModularCore:<module>:<core_version>`, from the repository `https://jitpack.io` with a content filter for that group only; each mod's `build.gradle` declares it for its standalone build, the root `build.gradle` for every workspace project (OMLib's `api` dependency reaches all of them).
- **In the jar:** a non-transitive `coreBundle` configuration copies the module's classes and its JvmDowngrader stubs (`omtteam/<mod>/core/**`, `omtteam/<mod>/jvmdg/**`) into the dev jar and so into `reobfJar`; players still get one jar per mod, and no jar carries another mod's core.
- **Local core:** when `../OpenModularCore` exists, the workspace `settings.gradle` and each mod's own settings file include it (`includeBuild`; the settings look for the first `OpenModularCore/` in a folder above them), and Gradle substitutes the JitPack modules with its projects (`project ':OpenModularCore:omt-core-j8' (by composite build)` in `dependencyInsight`).
  A core edit then reaches every build at once, without a release; the settings log `OpenModularCore: local source at …`.
  `-Pomm.noLocalCore` or `OMM_NO_LOCAL_CORE=1` turns it off: the builds then take the JitPack artifact of `core_version`, as a build without `../OpenModularCore` (CI) does.
  Never run a build of this workspace and one of a modern workspace (`../OMTTeam1.20.1`, `../OMTTeam1.21.1`, `../OMTTeam1.21.11`) at the same time while both use the local core: they would build into the same `OpenModularCore` build folders (set `OMM_NO_LOCAL_CORE=1` for one of them).
- **Same version everywhere:** `../check-core-version.sh` compares `core_version` in every mod of every workspace and fails on a difference; `--release` also requires a local `../OpenModularCore` (unless turned off) to be clean and at the tag `core_version`; `release.sh` runs it first.
- **Changing the core:** edit and check it in `../OpenModularCore` (`./gradlew check` there), then build this workspace and the modern ones against it (`../CLAUDE.md`, "Changing the core"); a core change for 1.12 alone does not exist.

## Core checks in the mods (from "One build per mod")

`verifyCoreIsolation`, the JaCoCo core report (both part of `check`) and `pitest` only run in a mod with a core package of its own; since the shared core no 1.12 mod has one, so they are skipped (`pitest` logs "no core package, nothing to mutate").
The core's isolation rules, coverage and mutation testing run in `../OpenModularCore` (`./gradlew check`, `./gradlew :<module>:pitest`, thresholds in its modules' `gradle.properties`).
Mutation thresholds (`pitest_mutation_threshold`) are now in OpenModularCore's modules: raise one when the score rises; lower it only on purpose, after checking that the new survivors are equivalent mutants and not missing tests.

## Status and open items (the OMLib parts, from "Status and open items")

### Decisions that still apply

- OMLib's `TrustedPlayersManager` / `ITrustedPlayersManager` and `PlayerUtil` keep their 3.x signatures and forward to the core `TrustManager`; `OMEnergyStorage` is the Forge adapter around the core `EnergyStore`; `EnumMachineMode` / `util.TargetingSettings` are 3.x facades over `core.machine`.
- 1.12 extras kept behind `SecurityEnvironment`: shared owners (`/omshareowner`) and offline-mode name matching; global trust lists removed.
- Ownership approach A: unowned bases stay; `IHasOwner.getOwner()` keeps its non-null 3.x contract, `api.permission.IOwnedHost.getOwnerOrNull()` is the 1.20.1+ form.

### Open

- **Rifted Valkyrium ships (branch `rv-compat` of OMLib and OMT, parked, no PR):** approach "ship space" (decided by the user): OMLib's `compat.valkyrien.ShipFrame` / `ShipFrames.at(world, pos)` converts targets into the ship's block grid, compiled against API stubs in OMLib's `src/api/java`.
  Open: a client test on a ship (RV 2.1.0 crashes dedicated servers); the core's `TargetEntity` eye position (`getEyeX/Y/Z`) must go through the `ShipFrame` too.
- **Backlog:** bloom for rays: OMLib's `client.render.object.Ray` keeps the `bloom` flag but `render` ignores it (`TODO: bloom`).
