import groovy.json.JsonSlurper
import java.io.File
import java.util.zip.ZipFile

/**
 * The jar-in-jar mods nested in the Fabric mod [jar], at every depth, extracted into [into] (kept between builds, written
 * again only when a file changed size).
 * Loom strips the nested jars from its remapped copies of mod dependencies and expects them as Maven dependencies, which
 * a jar from Modrinth's maven does not declare; adding these files to a mod configuration (`modLocalRuntime`) puts them
 * into the dev runs and the game test server, where Fabric Loader loads them as mods.
 * Only the jars that a `fabric.mod.json` lists under `"jars"` count (a jar may carry other files in `META-INF/jars`).
 */
fun nestedModJars(jar: File, into: File): List<File> {
    val found = mutableListOf<File>()
    ZipFile(jar).use { zip ->
        val metadata = zip.getEntry("fabric.mod.json") ?: return found
        val json = zip.getInputStream(metadata).use { JsonSlurper().parse(it) } as? Map<*, *>
        val listed = (json?.get("jars") as? List<*>).orEmpty().mapNotNull { (it as? Map<*, *>)?.get("file") as? String }
        for (path in listed) {
            val entry = zip.getEntry(path) ?: continue
            val target = into.resolve(path.substringAfterLast('/'))
            if (!target.isFile || target.length() != entry.size) {
                into.mkdirs()
                zip.getInputStream(entry).use { input -> target.outputStream().use { input.copyTo(it) } }
            }
            found += target
            found += nestedModJars(target, into)
        }
    }
    return found
}
