pluginManagement {
    // Shared build logic (the mod-common convention plugin), the same one the mods use from this repository
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Auto-provisions the Java toolchain (17) if it is not installed locally
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // Node name = "<minecraft version>-<loader>"; the build script follows the loader. The same nodes as the mods.
        versions("1.20.1-fabric" to "1.20.1", "1.20.1-forge" to "1.20.1")
        mapBuilds { _, node -> "build.${node.project.substringAfterLast('-')}.gradle.kts" }
        vcsVersion = "1.20.1-fabric" // the node the committed source is written for
    }
}

rootProject.name = "omlib"

// OpenModularCore from source (CLAUDE.md of the OMM root, "Local development"): when a folder above this one
// holds OpenModularCore/, that build replaces the JitPack modules com.github.OpenModularTurretsTeam.OpenModularCore:<module>
// (its group is the JitPack group, its project names are the module names). -Pomm.noLocalCore or OMM_NO_LOCAL_CORE=1
// turns this off: the build then takes the JitPack artifact of core_version, as CI does (CI has no OpenModularCore/).
// The same block is in the mods' settings and the workspace's; tools/bootstrap.sh finds the folder the same way.
val localCoreOff = providers.gradleProperty("omm.noLocalCore").getOrElse("false") != "false" ||
    !(System.getenv("OMM_NO_LOCAL_CORE") ?: "0").matches(Regex("0?"))
if (!localCoreOff) {
    generateSequence(settingsDir) { it.parentFile }
        .map { File(it, "OpenModularCore") }
        .firstOrNull { File(it, "settings.gradle.kts").isFile }
        ?.let { localCore ->
            logger.lifecycle("OpenModularCore: local source at $localCore (OMM_NO_LOCAL_CORE=1 for the JitPack artifact)")
            includeBuild(localCore)
        }
}
