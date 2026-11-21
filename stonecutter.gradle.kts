plugins {
    id("dev.kikugie.stonecutter")
    // Loader plugins, applied by build.fabric.gradle.kts / build.neoforge.gradle.kts / build.forge.gradle.kts
    // (the publish plugin comes from build-logic, see build-logic/build.gradle.kts)
    id("net.fabricmc.fabric-loom-remap") version "1.18.4" apply false
    // Forge 1.20.1: the legacy variant of ModDevGradle (same artifact; reobfuscates to SRG names)
    id("net.neoforged.moddev.legacyforge") version "2.0.148" apply false
}

stonecutter active "1.20.1-fabric"

// Loader constants for conditions: //? if fabric { ... //? if neoforge { ... //? if forge {
// Prefer whole classes in platform/<loader>/ packages (left out of the other loaders' builds) over conditions.
stonecutter parameters {
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
}

// Builds every version in one go: ./gradlew chiseledBuild
tasks.register("chiseledBuild") {
    group = "build"
    description = "Builds the library for all Stonecutter versions"
    dependsOn(stonecutter.versions.map { ":${it.project}:build" })
}

// Checks every version: from the workspace, :<repo>:check names only this (root) project's check, which the nodes' tests
// need to hang on; inside this folder ./gradlew check runs them anyway (Gradle runs each task once)
tasks.register("check") {
    group = "verification"
    description = "Runs the checks of all Stonecutter versions"
    dependsOn(stonecutter.versions.map { ":${it.project}:check" })
}

// Puts the jar of every version into build/maven (what the mods' nested build does per version): ./gradlew chiseledPublishLocal
tasks.register("chiseledPublishLocal") {
    group = "publishing"
    description = "Publishes the library for all Stonecutter versions to the local repository in build/maven"
    dependsOn(stonecutter.versions.map { ":${it.project}:publishAllPublicationsToProjectRepository" })
}

// Uploads every version to CurseForge / Modrinth (only with project ids and tokens set, see mod-common): ./gradlew chiseledPublish
tasks.register("chiseledPublish") {
    group = "publishing"
    description = "Publishes the library for all Stonecutter versions to CurseForge / Modrinth"
    dependsOn(stonecutter.versions.map { ":${it.project}:publishMods" })
}
