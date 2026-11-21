# OMLib

Repo for [OMLib](https://www.curseforge.com/minecraft/mc-mods/omlib) (Open Modular Lib) for Minecraft 1.12.2 (Forge).

OMLib is the shared library of the Open Modular mods [OpenModularTurrets](https://www.curseforge.com/minecraft/mc-mods/openmodularturrets), [OpenModularPassiveDefense](https://www.curseforge.com/minecraft/mc-mods/ompd) and [OpenModularLighting](https://github.com/OpenModularTurretsTeam/OpenModularLighting).
It adds no gameplay of its own; install it when one of those mods requires it.

It provides what the mods have in common:

- **Ownership and trust lists:** machine owners, trusted players with access levels, scoreboard teams counting as trusted
- **Camouflage:** the camouflage menu to disguise blocks as other blocks, with light value and light opacity sliders
- **Memory card rules** for copying settings between machines
- **Energy:** an energy buffer with Forge Energy, Redstone Flux and Tesla support
- **Shared blocks, tile entities, GUIs and rendering helpers**, and the integrations with The One Probe, HWYLA and OpenComputers

If you need help, ask us in our [Discord](https://discord.gg/SEG695u).
Changes are listed in [CHANGELOG.md](CHANGELOG.md).

## Building

OMLib for 1.12.2 is built with RetroFuturaGradle; Gradle runs on Java 25, and the mod is compiled with a Java 8 toolchain that Gradle downloads if it is missing.

```bash
git clone -b 1.12 https://github.com/OpenModularTurretsTeam/OMLib.git
cd OMLib
./gradlew build                  # jars in build/libs/
```

[CONTRIBUTING.md](CONTRIBUTING.md) covers the development setup: tests, dev runs, a local checkout of the shared core (OpenModularCore), and how to build the other mods against a locally built OMLib.
[docs/architecture.md](docs/architecture.md) is a guide to the code.

## License

The projects source code is licensed under the [MIT license](LICENSE), the projects art is licenced under the [CC-BY-NC-SA 4.0 license](LICENSE-ART) (see [LICENSE-DUAL](LICENSE-DUAL)).
