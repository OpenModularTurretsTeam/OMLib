// Fabric nodes (Loom). Shared logic (Java, metadata templating, tests, publishing) is in build-logic/mod-common.
plugins {
    id("mod-common")
    id("net.fabricmc.fabric-loom-remap")
    `maven-publish`
    // Test coverage of the library, from the unit tests
    jacoco
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

    // NEEPMeat (1.20.1 only; the other nodes have no neepmeat_version): its vascular network powers the transfer
    // hosts (FabricNeepMeatPower in src/main/mc1.20.1-1.20.1), which is only loaded when NEEPMeat is installed
    (findProperty("neepmeat_version") as String?)?.let { modCompileOnly("maven.modrinth:neepmeat:$it") }
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

// ---- Test coverage (JaCoCo): check prints the line coverage per package ----
// Only the unit tests record coverage: the library has no game tests, and the mods' game tests run it in their builds.
// Active version only (all versions share the source); platform/neoforge and platform/forge are not on a Fabric node.
// Report: versions/<v>/build/reports/jacoco/test/html/index.html
val coverageActive = stonecutter.current.isActive
jacoco {
    toolVersion = property("jacoco_version") as String
}
tasks.test {
    extensions.configure<JacocoTaskExtension> { isEnabled = coverageActive }
}
tasks.jacocoTestReport {
    enabled = coverageActive
    dependsOn(tasks.test)
    sourceDirectories.setFrom(sourceSets.main.get().java.srcDirs)
    classDirectories.setFrom(sourceSets.main.get().output.classesDirs)
    reports {
        csv.required = true
        xml.required = true
        html.required = true
    }
    val csv = reports.csv.outputLocation
    val html = reports.html.outputLocation
    doLast {
        // line coverage per package, and of the whole library
        val rows = csv.get().asFile.readLines().drop(1).map { it.split(',') }
        val byPackage = sortedMapOf<String, IntArray>()
        for (row in rows) {
            val name = row[1].removePrefix("omtteam.omlib").removePrefix(".").ifEmpty { "(omlib)" }
            val lines = byPackage.getOrPut(name) { IntArray(2) }
            lines[0] += row[7].toInt() // LINE_MISSED
            lines[1] += row[8].toInt() // LINE_COVERED
        }
        fun percent(l: IntArray) = "%5.1f%%  (%d/%d lines)".format(100.0 * l[1] / maxOf(1, l[0] + l[1]), l[1], l[0] + l[1])
        val total = IntArray(2).also { t -> byPackage.values.forEach { t[0] += it[0]; t[1] += it[1] } }
        println("Test coverage (unit tests):")
        println("  %-26s %s".format("all", percent(total)))
        byPackage.forEach { (name, lines) -> println("    %-24s %s".format(name, percent(lines))) }
        println("  Report: ${html.get().asFile.resolve("index.html").toURI()}")
    }
}
tasks.check {
    dependsOn(tasks.jacocoTestReport)
}

// ---- Mutation testing (PIT): ./gradlew :1.21.1-fabric:pitest, on demand ----
// Plants small mistakes (flipped conditions, changed boundaries, removed calls) and checks whether a unit test
// fails. Surviving mistakes show behaviour the tests do not pin down. Report only, not part of check.
// Only the code the unit tests are written for: the rest (blocks, screens, menus, packet handlers, platform) needs a
// running game, which the mods' game tests cover. Known survivors that no test can kill: README.md, "Building".
// Report: versions/<v>/build/reports/pitest/index.html
val pitest = configurations.create("pitest") {
    isCanBeConsumed = false
}
dependencies {
    pitest("org.pitest:pitest-command-line:${property("pitest_version")}")
    pitest("org.pitest:pitest-junit5-plugin:${property("pitest_junit5_version")}")
}
val pitestTargets = listOf(
    "omtteam.omlib.core.*",
    "omtteam.omlib.config.*",
    "omtteam.omlib.compat.nbt.*",
    "omtteam.omlib.compat.network.PacketType",
    "omtteam.omlib.menu.LongContainerData",
    "omtteam.omlib.network.CamoPackets*",
    "omtteam.omlib.network.ConfigSyncPacket",
    "omtteam.omlib.network.TrustPackets*",
    "omtteam.omlib.security.SecurityNbt",
)
tasks.register<JavaExec>("pitest") {
    group = "verification"
    description = "Mutation testing of the unit-tested library code (report in build/reports/pitest)"
    dependsOn(tasks.testClasses)
    classpath = pitest + sourceSets.test.get().runtimeClasspath
    mainClass = "org.pitest.mutationtest.commandline.MutationCoverageReport"
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
    val reportDir = layout.buildDirectory.dir("reports/pitest")
    val sources = sourceSets.main.get().java.srcDirs
    outputs.dir(reportDir)
    // fail on a degradation: % of mutations detected, % line coverage of the mutated classes, mutations not detected
    // (survived or not reached by any test); the values are in gradle.properties
    val thresholds = listOf(
        "--mutationThreshold", project.property("pitest_mutation_threshold").toString(),
        "--coverageThreshold", project.property("pitest_coverage_threshold").toString(),
        "--maxSurviving", project.property("pitest_max_surviving").toString(),
    )
    inputs.property("thresholds", thresholds)
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "--reportDir", reportDir.get().asFile.path,
            "--targetClasses", pitestTargets.joinToString(","),
            "--targetTests", "omtteam.omlib.*",
            // the test configs (config.mod) share the config package
            "--excludedClasses", "*Test,*Test\$*,omtteam.omlib.config.mod.*",
            "--sourceDirs", sources.joinToString(","),
            "--threads", Runtime.getRuntime().availableProcessors().toString(),
            "--outputFormats", "HTML,XML",
            "--timestampedReports=false",
        ) + thresholds
    })
    doLast {
        println("PIT report: ${reportDir.get().asFile.resolve("index.html").toURI()}")
    }
}
