# Contributing to OMLib

Thanks for helping out!
This branch (`1.12`) holds OMLib for Minecraft 1.12.2 on Forge, the shared library of OpenModularTurrets, OpenModularPassiveDefense and OpenModularLighting.
It is built with [RetroFuturaGradle](https://github.com/GTNewHorizons/RetroFuturaGradle) on current Gradle.

Questions are welcome on our [Discord](https://discord.gg/SEG695u).

## Reporting issues

Open an issue on [GitHub](https://github.com/OpenModularTurretsTeam/OMLib/issues).
Include the Minecraft, Forge and mod versions (OMLib and the mods that use it), and your log or crash report.

## Development setup

You need:

- **Java 25** to run Gradle (RetroFuturaGradle 2.0.6 needs it).
  Set `JAVA_HOME` to it, or set `org.gradle.java.home` in `~/.gradle/gradle.properties`; in IntelliJ IDEA, set the Gradle JVM to 25.
- The mod is compiled, tested and run with an **Azul Java 8** toolchain.
  Gradle downloads it on the first build if it is missing (another vendor's Java 8 does not count).
- Git and an IDE with Gradle support (IntelliJ IDEA is recommended).

### Building

```bash
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OMLib.git
cd OMLib
./gradlew build                  # compile, unit tests, jars
```

The first build downloads Gradle 9.8.1, the Java 8 toolchain, Minecraft 1.12.2 and Forge 14.23.5.2847 (MCP mappings `stable_39`), and decompiles them; this takes a while.

The jars go to `build/libs/`:

- `omlib-<version>-mc1.12.2-forge.jar`: the jar players install (SRG names).
- `omlib-<version>-mc1.12.2-forge-dev.jar`: the dev jar (MCP names), which the mods compile against.
- `omlib-<version>-mc1.12.2-forge-sources.jar`: the sources.

### Useful commands

```bash
./gradlew build                  # compile, unit tests, jars
./gradlew test                   # unit tests only
./gradlew runClient              # start a client with OMLib in run/
./gradlew runServer              # start a dedicated server in run/
./gradlew setupDecompWorkspace   # decompiled Minecraft and Forge sources in build/rfg/minecraft-src
./gradlew dependencyMap          # package dependency map in build/reports/dependency-map/index.html
./gradlew publishToMavenLocal    # build, then publish OMLib to your local Maven repository (see below)
```

`runClient` logs in as `Developer`; pass `--username=<name>` for another name.
`run/` is not tracked by git.
The first `runServer` asks on the console whether to start in online mode (answer `n`, so a dev client can join) and whether you accept the Minecraft EULA (answer `y`, or the task stops).
It writes `run/server.properties` and `run/eula.txt` from your answers.
If you create those two files yourself first (`online-mode=false`, `eula=true`), it asks nothing.
OMLib alone adds no gameplay; to see a change in game, run a mod with your OMLib (see "Testing OMLib with the mods").

## The shared core (OpenModularCore)

The game rules in `omtteam.omlib.core` are not in this repository.
They come from [OpenModularCore](https://github.com/OpenModularTurretsTeam/OpenModularCore), module `omlib-core-j8` (downgraded to Java 8), from JitPack in the version `core_version` in `gradle.properties`.
The build copies the core classes (`omtteam/omlib/core`, `omtteam/omlib/jvmdg`) into the OMLib jars, so players still install one jar.

To change the core and test it here, clone OpenModularCore next to the OMLib folder (or in any folder above it):

```
<your folder>/
├── OpenModularCore/
└── OMLib/                # branch 1.12
```

```bash
cd ..                            # from OMLib/ to the folder that holds it
git clone https://github.com/OpenModularTurretsTeam/OpenModularCore.git
cd OpenModularCore
./gradlew check                  # the core's tests on Java 17 and Java 8, and its checks
cd ../OMLib
./gradlew build                  # now builds against the local core
```

`settings.gradle` looks for a folder named `OpenModularCore` that contains a `settings.gradle.kts`, in this folder and in every folder above it, and builds against the first one it finds instead of the JitPack artifact, whatever `core_version` says.
Gradle then prints `OpenModularCore: local source at <path> (OMM_NO_LOCAL_CORE=1 for the JitPack artifact)`.
No line is printed when the core comes from JitPack.
A folder without `settings.gradle.kts` (for example one left by a failed clone) is skipped without a message.

To build against the JitPack artifact of `core_version` again, as a release does:

```bash
OMM_NO_LOCAL_CORE=1 ./gradlew build   # in OMLib/; or: ./gradlew build -Pomm.noLocalCore
```

Rule changes and their unit tests go to OpenModularCore, not here.
A core change reaches the released mods only after a new core release, when `core_version` is raised in every mod.

## Testing OMLib with the mods

The mods build against OMLib's dev jar from your local Maven repository (`~/.m2/repository`) when asked to.
First publish your OMLib:

```bash
./gradlew publishToMavenLocal    # in OMLib/: build (with tests), then publish
```

This publishes the jars as `omtteam:omlib:1.12.2-<version>` (with the classifiers `dev` and `sources`).
`<version>` is `mod_version` from `gradle.properties`, followed by `-alpha.<release_number>` or `-beta.<release_number>` when `release_type` is `alpha` or `beta` (the same version as in the jar names).
Use that same `<version>` for `-Pomlib_version` below.
It replaces an OMLib of the same version that is already in your local Maven repository.

Clone the mods next to OMLib, each on its branch `1.12`:

```bash
cd ..                            # next to OMLib/ (and OpenModularCore/, if you have it)
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OpenModularTurrets.git
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OpenModularPassiveDefense.git
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OpenModularLighting.git
```

Next to an OpenModularCore checkout, the mods build their own core module from it too.
Then build the mod in its own folder, naming the OMLib version you published:

```bash
./gradlew build -Pomlib_local                           # OpenModularTurrets, when its omlib_version matches
./gradlew build -Pomlib_local -Pomlib_version=<version> # OpenModularPassiveDefense
./gradlew build -Pomlib_version=<version>               # OpenModularLighting (always takes OMLib from the local Maven repository)
```

- Without `-Pomlib_local`, OpenModularTurrets and OpenModularPassiveDefense build against an older OMLib release from CurseForge (`omlib_curse_file_id` in their `gradle.properties`), which lacks the current API.
- Each mod names the OMLib version it builds against as `omlib_version` in its `gradle.properties`.
  If that is not the version you published, pass `-Pomlib_version=<version>`; it also sets the OMLib version range the mod requires at runtime.
- The published dev jar carries its own copy of OMLib's core.
  After a change to OMLib's core in OpenModularCore, publish OMLib again before you build the mods.

OpenModularLighting is the only mod whose standalone dev runs load the OMLib you published, because it takes OMLib on its runtime classpath:

```bash
./gradlew runClient -Pomlib_version=<version>   # in OpenModularLighting/: a client with your OMLib and OML
```

OpenModularTurrets and OpenModularPassiveDefense only compile against OMLib, so their `runClient` and `runServer` do not load it.
To try them, copy your `build/libs/omlib-<version>-mc1.12.2-forge.jar` (SRG names; FML remaps it at runtime) into the mod's `run/mods/` folder and start the mod's `runClient` or `runServer`, as its CONTRIBUTING.md describes.

## Code layout

- `src/main/java` holds the library, `src/api/java` the bundled ComputerCraft API (compiled against, not part of the jar), `src/test/java` the unit tests.
- Client-only code (GUIs, renderers, the camouflage model) goes into `omtteam.omlib.client` and is reached from common code only through the proxy.
- [docs/architecture.md](docs/architecture.md) describes the layers, where things live and how to add a block, a packet or a config value.

## Code style

- Java 8, 4-space indent, no wildcard imports.
- MCP names of Minecraft 1.12.2 (`TileEntity`, `EntityPlayer`, …).
- All user-facing text goes through the lang files in `src/main/resources/assets/omlib/lang` (`en_US.lang` first).
- Gameplay logic runs on the server.
  Every client-to-server packet must check permissions again on the server.
- The mods call OMLib's public classes and methods, also from jars that are already released.
  Don't remove or change them without need, and list API changes in the changelog.
- Items that have no recipe on purpose are listed in `recipe-exempt.txt`, one `<item id> # <reason>` per line.
- There is no formatter in this build; follow the style of the surrounding code.

## Pull requests

1. Branch off `1.12` and keep each pull request focused on one change.
2. Add a one-line entry for every user-visible change under `## [Unreleased]` in `CHANGELOG.md`, by hand or with:

   ```bash
   ./gradlew changelogEntry -Ptype=fixed -Pentry="Trusted players keep their access level after a server restart"
   ```

   Types are `added`, `changed`, `fixed` and `removed`.
   Start API changes with `API:`.
3. Make sure `./gradlew build` passes, which runs the unit tests and the architecture tests (`ArchitectureTest`, frozen rules in `src/test/resources/archunit_store`).
4. If you can, test in game with the mods that use the changed code (see "Testing OMLib with the mods"), and on a dedicated server for anything involving networking or GUIs.
5. Describe what you changed and how you tested it in the pull request.

Releases are made by the team.

## License

By contributing, you agree that your code is licensed under the [MIT license](LICENSE) and your art (textures, models, sounds) under [CC BY-NC-SA 4.0](LICENSE-ART), as [LICENSE-DUAL](LICENSE-DUAL) describes.
