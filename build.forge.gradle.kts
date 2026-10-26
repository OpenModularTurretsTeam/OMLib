// Forge nodes (1.20.1; legacy ModDevGradle). Shared logic (Java, metadata templating, tests, publishing) is in
// build-logic/mod-common. Forge 1.20.1 runs on SRG names: the jar is reobfuscated (reobfJar), and the mods take that
// jar and remap it to Mojang names through their mod* configurations.
plugins {
    id("mod-common")
    id("net.neoforged.moddev.legacyforge")
    `maven-publish`
}

val modId = property("mod_id") as String
val modName = property("mod_name") as String

// Client-only code keeps its own source set (src/client), like Loom's split source sets on Fabric:
// main cannot see client classes, and the jar contains both.
val client: SourceSet = sourceSets.create("client") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

legacyForge {
    version = property("forge_version") as String
    addModdingDependenciesTo(client)
    addModdingDependenciesTo(sourceSets.test.get()) // unit tests use game libraries (Gson), as on Loom

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(client)
        }
    }
}

// Forge 1.20.1 only loads a mod's resources as a pack if it has a pack.mcmeta (Fabric and NeoForge 1.21 need none, and
// a shared one would carry the wrong pack_format there), so this node generates it.
val packMetadata = tasks.register("modPackMetadata") {
    group = "build"
    val output = layout.buildDirectory.dir("generated/modPackMetadata")
    outputs.dir(output)
    doLast {
        val text = """{ "pack": { "description": "$modName resources", "pack_format": 15 } }""" + "\n"
        output.get().file("pack.mcmeta").asFile.apply { parentFile.mkdirs() }.writeText(text)
    }
}
sourceSets.main { resources.srcDir(packMetadata) }

tasks.jar { from(client.output) }
tasks.named<Jar>("sourcesJar") { from(client.allSource) }

// The mods build this library as a nested build and take the jar from this local repository (build/maven): the
// reobfuscated one (SRG names), which their legacy ModDevGradle remaps for the dev runs, and the same jar with the dev
// names (classifier dev) for the game test runs, which have no remapping
publishing {
    publications {
        create<MavenPublication>("omlib") {
            artifactId = "omlib"
            artifact(tasks.named<Jar>("reobfJar"))
            artifact(tasks.named<Jar>("jar")) { classifier = "dev" }
            artifact(tasks.named<Jar>("sourcesJar")) { classifier = "sources" }
        }
    }
    repositories {
        maven {
            name = "project"
            url = uri(rootProject.layout.buildDirectory.dir("maven"))
        }
    }
}

publishMods {
    // the reobfuscated jar (SRG names), the one Forge loads outside the dev environment
    file = tasks.named<Jar>("reobfJar").flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
}
