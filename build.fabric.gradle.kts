// Fabric nodes (Loom). Shared logic (Java, metadata templating, tests, publishing) is in build-logic/mod-common.
plugins {
    id("mod-common")
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
}

val modId = property("mod_id") as String
val minecraftVersion = property("minecraft_version") as String

loom {
    splitEnvironmentSourceSets()

    mods {
        register(modId) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }
}

// The client source set has no resources, so Gradle never creates build/resources/client, but Loom still puts that
// folder on the classpath of every game run and Fabric Loader warns about it. Creating the empty folder avoids it.
val clientResources = layout.buildDirectory.dir("resources/client")
tasks.withType<net.fabricmc.loom.task.AbstractRunTask>().configureEach {
    doFirst { clientResources.get().asFile.mkdirs() }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    // Energy API (jar-in-jar; the mods get it through this library)
    modImplementation("teamreborn:energy:${property("team_reborn_energy_version")}")
    include("teamreborn:energy:${property("team_reborn_energy_version")}")
}

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
    file = tasks.remapJar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.remapSourcesJar.flatMap { it.archiveFile })
}
