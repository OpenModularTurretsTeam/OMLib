plugins {
    id("dev.kikugie.stonecutter")
    // Loader plugins, applied by build.fabric.gradle.kts / build.neoforge.gradle.kts / build.forge.gradle.kts
    // (the publish plugin comes from build-logic, see build-logic/build.gradle.kts)
    id("net.fabricmc.fabric-loom-remap") version "1.18.4" apply false
    id("net.neoforged.moddev") version "2.0.148" apply false
    // Forge 1.20.1: the legacy variant of ModDevGradle (same artifact; reobfuscates to SRG names)
    id("net.neoforged.moddev.legacyforge") version "2.0.148" apply false
}

stonecutter active "1.21.1-fabric"

// Loader constants for conditions: //? if fabric { ... //? if neoforge { ... //? if forge {
// Prefer whole classes in platform/<loader>/ packages (left out of the other loaders' builds) over conditions.
// The replacements are the same as in the mods, so shared code reads the same in both.
stonecutter parameters {
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
    // Renames from 1.21.11. They work both ways, so the new names (Identifier, noCollision) must not be used for anything
    // else in the source. 1.21.11 renamed ResourceLocation to Identifier (same package).
    replacements.regex(current.parsed >= "1.21.11") {
        replace("\\bResourceLocation\\b", "Identifier", "\\bIdentifier\\b", "ResourceLocation")
    }
    // 1.21.11 renamed the slowness effect, the horse package, Entity#hasImpulse and #moveTo, and moved Util
    replacements.regex(current.parsed >= "1.21.11") {
        replace("\\bMOVEMENT_SLOWDOWN\\b", "SLOWNESS", "\\bSLOWNESS\\b", "MOVEMENT_SLOWDOWN")
        replace("\\banimal\\.horse\\b", "animal.equine", "\\banimal\\.equine\\b", "animal.horse")
        replace("\\bhasImpulse\\b", "needsSync", "\\bneedsSync\\b", "hasImpulse")
        replace("\\bmoveTo\\(", "snapTo(", "\\bsnapTo\\(", "moveTo(")
        replace("\\bnet\\.minecraft\\.Util\\b", "net.minecraft.util.Util", "\\bnet\\.minecraft\\.util\\.Util\\b", "net.minecraft.Util")
    }
    // 1.21.11 fixed the spelling of BlockBehaviour.Properties#noCollission
    replacements.regex(current.parsed >= "1.21.11") {
        replace("\\bnoCollission\\(", "noCollision(", "\\bnoCollision\\(", "noCollission(")
    }
}

// Builds every version in one go: ./gradlew chiseledBuild
tasks.register("chiseledBuild") {
    group = "build"
    description = "Builds the library for all Stonecutter versions"
    dependsOn(stonecutter.versions.map { ":${it.project}:build" })
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
