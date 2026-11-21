// Build logic shared by every Stonecutter node, whatever its loader. The loader build scripts
// (build.fabric.gradle.kts, build.neoforge.gradle.kts, build.forge.gradle.kts) apply this first, then their loader plugin.
// Mod-independent: all names come from gradle.properties, so every mod uses this build logic as it is (the omlib submodule).
import java.util.Properties

plugins {
    // java-library for compileOnlyApi (the library publishes its core module for compiling, see OpenModularCore below)
    `java-library`
    id("me.modmuss50.mod-publish-plugin")
}

/** The loaders this setup knows; the node name ends with one of them ("1.21.1-neoforge"). */
val loaders = listOf("fabric", "neoforge", "forge")
val loader = project.name.substringAfterLast('-')
require(loader in loaders) { "Stonecutter node '${project.name}' must end with one of $loaders" }

val modId = property("mod_id") as String
val modName = property("mod_name") as String
val minecraftVersion = property("minecraft_version") as String
val javaVersion = (property("java_version") as String).toInt()
/** The base package, derived from maven_group (they are the same in our mods). */
val basePackage = property("maven_group") as String

version = "${property("mod_version")}+$minecraftVersion-$loader"
group = basePackage
base.archivesName = property("archives_base_name") as String
// Jar names are <archives_base_name>-<mod_version>-mc<minecraft version>-<loader>; the mod metadata keeps the "+" version
val archiveVersionName = "${property("mod_version")}-mc$minecraftVersion-$loader"
tasks.withType<AbstractArchiveTask>().configureEach { archiveVersion = archiveVersionName }

repositories {
    // Optional integrations such as Jade, only from Modrinth's maven
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
}

// ---- OMLib: the library submodule (omlib), built before the mod and bundled into the jar ----
// The settings script of a mod builds the git submodule `omlib` into omlib/build/maven (omlib/tools/bootstrap.sh); the
// mod's nodes depend on the jar of their own node from there, and the loader scripts bundle it (Fabric include,
// NeoForge/Forge jarJar). They use extra["omlibCoordinates"]; extra["omlibVersion"] is the library's mod_version, for the
// mod metadata. The library itself has no such dependency.

val isLibrary = modId == "omlib"
extra["omlibVersion"] = ""
extra["omlibCoordinates"] = ""
if (!isLibrary) {
    val omlibDir = rootProject.file("omlib")
    val omlibProperties = Properties()
    omlibDir.resolve("gradle.properties").takeIf { it.exists() }?.inputStream()?.use { omlibProperties.load(it) }
        ?: throw GradleException("The omlib submodule is missing: run `git submodule update --init`")
    val omlibModVersion = omlibProperties.getProperty("mod_version")
    val omlibNodeVersion = "$omlibModVersion+$minecraftVersion-$loader"
    extra["omlibVersion"] = omlibModVersion
    extra["omlibCoordinates"] = "omtteam.omlib:omlib:$omlibNodeVersion"

    repositories {
        maven(omlibDir.resolve("build/maven")) {
            name = "OMLib"
            content { includeGroup("omtteam.omlib") }
        }
    }
}

// ---- OpenModularCore: the shared game rules (plain Java, no Minecraft), from JitPack ----
// gradle.properties names the mod's core module (core_module: omlib-core, omt-core, oml-core) and core_version, which is
// the same in every mod and workspace (../CLAUDE.md of the workspaces' root, "Shared core logic"). Without core_module
// the mod has no core.
// The module's classes are extracted into build/core/classes, which counts as a classes folder of the main source set:
// - every jar starts from main's output, so the mod's jar bundles them on every loader (Loom remapJar, ModDevGradle
//   jar, legacy ModDevGradle jar and reobfJar; the core has no Minecraft names, so remapping leaves it alone);
// - the dev runs, game tests and unit tests load them as part of the mod (Loom's classpath group, ModDevGradle's mod
//   folders), so no loader needs a plain library on its classpath (NeoForge 1.21.1 and Forge would not load one).
// Only that module goes into the jar (omt-core's dependency on omlib-core comes with OMLib's jar), and only its
// <base package>.core classes. For compiling, the module is a compileOnly dependency (compileOnlyApi in the library: its
// published metadata names omlib-core for the mods' compile classpath, while the classes come with its jar); it is never
// a runtime dependency, which would put the same classes on the classpath twice.
val coreGroup = "com.github.OpenModularTurretsTeam.OpenModularCore"
val coreModule = (findProperty("core_module") as String?)?.takeIf { it.isNotBlank() }
val corePath = "${basePackage.replace('.', '/')}/core"
if (coreModule != null) {
    val coreVersion = (findProperty("core_version") as String?)?.takeIf { it.isNotBlank() }
        ?: throw GradleException("core_module is set, so gradle.properties needs core_version too")
    val coreDependency = "$coreGroup:$coreModule:$coreVersion"

    repositories {
        // JitPack builds OpenModularCore from its git tags; it is asked for nothing else
        maven("https://jitpack.io") {
            name = "JitPack"
            content { includeGroup(coreGroup) }
        }
    }

    dependencies {
        add(if (isLibrary) "compileOnlyApi" else "compileOnly", coreDependency)
    }

    // The module's own jar, without its dependencies
    val coreBundle = configurations.create("coreBundle") {
        description = "The OpenModularCore module whose classes the jar bundles"
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
    dependencies { coreBundle(coreDependency) }

    val coreClasses = layout.buildDirectory.dir("core/classes")
    val extractCore = tasks.register<Sync>("extractCore") {
        group = "build"
        description = "Extracts the classes of $coreModule, which the main source set then holds as its own"
        from(coreBundle.elements.map { jars -> jars.map { zipTree(it.asFile) } })
        include("$corePath/**")
        into(coreClasses)
        doLast {
            if (destinationDir.resolve(corePath).walkTopDown().none { it.isFile && it.extension == "class" }) {
                throw GradleException("$coreDependency has no classes in $corePath")
            }
        }
    }
    sourceSets.main {
        (output.classesDirs as ConfigurableFileCollection).from(coreClasses).builtBy(extractCore)
    }

    // A POM knows no compile-only scope: Gradle writes compileOnlyApi as compile scope, which Maven consumers (and Gradle
    // ones where no .module file is published) also put on the runtime classpath, next to the classes in the jar. So the
    // POMs name no core module; the .module files keep it for compiling (apiElements only).
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom.withXml {
                    fun groovy.util.Node.children(name: String) =
                        children().filterIsInstance<groovy.util.Node>().filter { it.name().toString().endsWith(name) }
                    asNode().children("dependencies").forEach { dependencies ->
                        dependencies.children("dependency")
                            .filter { dependency -> dependency.children("groupId").any { it.text() == coreGroup } }
                            .forEach { dependencies.remove(it) }
                        if (dependencies.children().isEmpty()) dependencies.parent().remove(dependencies)
                    }
                }
            }
        }
    }
}

// Loader-specific classes live in .../platform/<loader>/ packages (main and client); other loaders' packages
// are left out of this node's compilation entirely, so they never need Stonecutter comments.
sourceSets.configureEach {
    java {
        (loaders - loader).forEach { exclude("**/platform/$it/**") }
    }
}

// One Minecraft version per branch: whole classes that differ between versions no longer live in version
// source folders (src/<source set>/mc<first>-<last>/java). Fail on one instead of leaving it out silently.
rootProject.file("src").listFiles()?.flatMap { it.listFiles()?.toList() ?: emptyList() }
    ?.filter { it.isDirectory && it.name.startsWith("mc") }
    ?.let { require(it.isEmpty()) { "Version source folders are not used on a one-version branch: $it" } }

dependencies {
    // Unit tests for the core game logic (test only, not shipped)
    testImplementation(platform("org.junit:junit-bom:${property("junit_version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

/** Mod metadata file of each loader; the others are left out of this node's jar. */
val metadataFiles = mapOf(
    "fabric" to "fabric.mod.json",
    "neoforge" to "META-INF/neoforge.mods.toml",
    "forge" to "META-INF/mods.toml",
)

tasks {
    processResources {
        // Values a metadata template may use; a key a loader does not have is empty
        val props = mapOf(
            "version" to project.version.toString(),
            "mod_id" to modId,
            "mod_name" to modName,
            "java_version" to javaVersion.toString(),
            "minecraft_dependency" to findProperty("minecraft_dependency")?.toString().orEmpty(),
            "loader_version" to findProperty("fabric_loader_version")?.toString().orEmpty(),
            "neoforge_version" to findProperty("neoforge_version")?.toString().orEmpty(),
            "neoforge_loader_range" to findProperty("neoforge_loader_range")?.toString().orEmpty(),
            "forge_dependency" to findProperty("forge_dependency")?.toString().orEmpty(),
            "forge_loader_range" to findProperty("forge_loader_range")?.toString().orEmpty(),
            "omlib_version" to project.extra["omlibVersion"].toString(),
        )
        inputs.properties(props)
        filesMatching(metadataFiles.getValue(loader)) { expand(props) }
    }

    // Every source set (main, test mods) may carry the metadata of all loaders; each jar keeps only its own
    withType<ProcessResources>().configureEach {
        (metadataFiles - loader).values.forEach { exclude(it) }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = javaVersion
    }

    test {
        useJUnitPlatform()
    }

    // The core game logic lives in OpenModularCore (its build checks that it stays free of Minecraft). A class in the
    // mod's own core package would end up next to the bundled core classes (or replace one of them), so the mod's
    // sources (any source set, any version folder) must have none.
    val verifyNoCoreSources = register("verifyNoCoreSources") {
        group = "verification"
        description = "Fails if the sources have files in the core package, which comes from OpenModularCore"
        val sources = rootProject.file("src")
        val root = rootProject.projectDir
        val coreFolder = "java/$corePath"
        inputs.dir(sources)
        doLast {
            val strays = sources.walkTopDown()
                .filter { it.isFile && it.parentFile.invariantSeparatorsPath.let { dir -> dir.endsWith("/$coreFolder") || dir.contains("/$coreFolder/") } }
                .map { it.relativeTo(root).invariantSeparatorsPath }.sorted().toList()
            if (strays.isNotEmpty()) {
                throw GradleException("The core package comes from OpenModularCore ($coreGroup:${coreModule ?: "<core_module>"}); " +
                    "move these files there or into the game layer:\n" + strays.joinToString("\n"))
            }
        }
    }

    // Every item of the mod has a recipe on this node, unless recipe-exempt.txt names it (see RecipeCoverage.kt);
    // reads the processed resources, so the data converted for NeoForge/Forge is what is checked there
    val checkRecipes = register("checkRecipes") {
        group = "verification"
        description = "Fails if an item has no recipe (and is not in recipe-exempt.txt) or a recipe makes an unknown item"
        val resources = processResources.map { it.destinationDir }
        val exemptions = rootProject.file("recipe-exempt.txt")
        inputs.files(processResources)
        inputs.files(exemptions)
        doLast {
            val problems = recipeCoverageProblems(resources.get(), modId, exemptions)
            if (problems.isNotEmpty()) {
                throw GradleException("Recipe check failed:\n" + problems.joinToString("\n"))
            }
        }
    }

    check {
        dependsOn(verifyNoCoreSources, checkRecipes)
    }

    jar {
        inputs.property("archivesName", base.archivesName)
        // License files bundled into the jar: bundled_licenses (comma-separated), default all three
        val licenses = (findProperty("bundled_licenses") as String? ?: "LICENSE,LICENSE-ART,LICENSE-DUAL")
            .split(',').map { it.trim() }
        from(rootProject.files(licenses)) {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}

// ---- Publishing to CurseForge / Modrinth ----
// The loader script sets `file` and `additionalFiles` (Loom: remapJar, ModDevGradle: jar, legacy ModDevGradle: reobfJar).

/** A token from the environment (CI) or ~/.gradle/gradle.properties (local); never from the repository. */
fun token(environmentVariable: String, gradleProperty: String): Provider<String> =
    providers.environmentVariable(environmentVariable).orElse(providers.gradleProperty(gradleProperty))

val curseforgeToken = token("CURSEFORGE_TOKEN", "curseforge_token")
val modrinthToken = token("MODRINTH_TOKEN", "modrinth_token")
val curseforgeProjectId = (findProperty("curseforge_project_id") as String?).orEmpty()
val modrinthProjectId = (findProperty("modrinth_project_id") as String?).orEmpty()
/** publish_enabled=false in a node's gradle.properties keeps that node a dry run, tokens or not. */
val publishEnabled = (findProperty("publish_enabled") as String?)?.toBoolean() ?: true
/** Mods the uploaded file requires on its loader. */
val requiredMods = if (loader == "fabric") listOf("fabric-api") else emptyList()

/** True if this site would upload without a token (an empty project id skips the site entirely). */
fun missingToken(projectId: String, token: Provider<String>): Provider<Boolean> =
    if (projectId.isEmpty()) providers.provider { false } else token.map { false }.orElse(true)

/**
 * The CHANGELOG.md section of this mod_version (without its heading). Run ./gradlew releaseChangelog first;
 * publishing fails if the section is missing.
 */
val releaseNotes: Provider<String> = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
    .map { text ->
        val version = property("mod_version") as String
        val headings = Regex("""(?m)^## \[([^\]]+)].*$""").findAll(text).toList()
        val heading = headings.firstOrNull { it.groupValues[1] == version }
            ?: throw GradleException("CHANGELOG.md has no section for $version; run ./gradlew releaseChangelog first")
        val end = headings.firstOrNull { it.range.first > heading.range.first }?.range?.first ?: text.length
        text.substring(heading.range.last + 1, end).trim()
    }

publishMods {
    // Shown like the jar file name (<archives_base_name>-<mod_version>-mc<minecraft version>-<loader>)
    displayName = "${base.archivesName.get()}-$archiveVersionName"
    version = project.version.toString()
    changelog = releaseNotes
    type = when (property("release_type")) {
        "release" -> STABLE
        "beta" -> BETA
        else -> ALPHA
    }
    modLoaders.add(loader)
    // Uploads only run for real when publishing is enabled and the tokens of all configured sites are present
    dryRun = missingToken(curseforgeProjectId, curseforgeToken)
        .zip(missingToken(modrinthProjectId, modrinthToken)) { curseforgeDry, modrinthDry ->
            !publishEnabled || curseforgeDry || modrinthDry
        }

    if (curseforgeProjectId.isNotEmpty()) {
        curseforge {
            projectId = curseforgeProjectId
            accessToken = curseforgeToken
            minecraftVersions.add(minecraftVersion)
            javaVersions.add(JavaVersion.toVersion(javaVersion))
            client = true
            server = true
            requiredMods.forEach { requires(it) }
        }
    }
    if (modrinthProjectId.isNotEmpty()) {
        modrinth {
            projectId = modrinthProjectId
            accessToken = modrinthToken
            minecraftVersions.add(minecraftVersion)
            environment = CLIENT_AND_SERVER
            requiredMods.forEach { requires(it) }
        }
    }
}
