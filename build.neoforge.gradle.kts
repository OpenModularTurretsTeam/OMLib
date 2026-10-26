// NeoForge nodes (ModDevGradle). Shared logic (Java, metadata templating, tests, publishing) is in build-logic/mod-common.
plugins {
    id("mod-common")
    id("net.neoforged.moddev")
    `maven-publish`
}

val modId = property("mod_id") as String

// Client-only code keeps its own source set (src/client), like Loom's split source sets on Fabric:
// main cannot see client classes, and the jar contains both.
val client: SourceSet = sourceSets.create("client") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

neoForge {
    version = property("neoforge_version") as String
    addModdingDependenciesTo(client)
    addModdingDependenciesTo(sourceSets.test.get()) // unit tests use game libraries (Gson), as on Loom

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
            sourceSet(client)
        }
    }
}

tasks.jar { from(client.output) }
tasks.named<Jar>("sourcesJar") { from(client.allSource) }

// The mods build this library as a nested build and take the jar from this local repository (build/maven)
publishing {
    publications {
        create<MavenPublication>("omlib") {
            artifactId = "omlib"
            from(components["java"])
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
    file = tasks.jar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
}
