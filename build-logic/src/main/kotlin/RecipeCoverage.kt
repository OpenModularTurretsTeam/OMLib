import groovy.json.JsonSlurper
import java.io.File

/**
 * Checks that the recipes of [namespace] in the processed resources [resources] of a node match its items.
 * The items are the item models (assets/<namespace>/models/item/<id>.json, so blocks without an item such as technical
 * blocks are left out); the recipes are data/<namespace>/recipe (1.21+) or recipes (1.20.1), any type, by their result.
 * [exemptions] is the mod's recipe-exempt.txt (one "<id> # <reason>" per line, `#` lines are comments; may be missing):
 * items that have no recipe on purpose (creative only, game masters only).
 * Returns the problems, empty if there are none:
 * - an item that no recipe produces and that is not exempt,
 * - a recipe whose result is an id of [namespace] without an item (left over after a rename or removal),
 * - an exemption without a reason, of an unknown item, or of an item that has a recipe after all.
 */
fun recipeCoverageProblems(resources: File, namespace: String, exemptions: File): List<String> {
    val problems = mutableListOf<String>()

    val items = resources.resolve("assets/$namespace/models/item").listFiles()
        ?.filter { it.isFile && it.extension == "json" }?.map { it.nameWithoutExtension }?.toSortedSet()
        ?: sortedSetOf()

    /** Recipe file (relative to its folder) -> result id; recipes without a plain result (special recipes) are skipped. */
    val results = sortedMapOf<String, String>()
    listOf("recipe", "recipes").map { resources.resolve("data/$namespace/$it") }.filter { it.isDirectory }.forEach { dir ->
        dir.walkTopDown().filter { it.isFile && it.extension == "json" }.forEach { file ->
            val result = (JsonSlurper().parse(file) as? Map<*, *>)?.get("result")
            // 1.20.1 cooking recipes: "result": "<id>"; crafting: {"item": ...} (1.20.1) or {"id": ...} (1.21+)
            val id = when (result) {
                is String -> result
                is Map<*, *> -> (result["id"] ?: result["item"]) as? String
                else -> null
            }
            if (id != null) results[file.relativeTo(dir).invariantSeparatorsPath] = id
        }
    }
    val crafted = results.values.filter { it.startsWith("$namespace:") }.map { it.substringAfter(':') }.toSet()

    val exempt = sortedSetOf<String>()
    if (exemptions.isFile) {
        exemptions.readLines().withIndex().forEach { (index, raw) ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach
            val id = line.substringBefore('#').trim()
            val where = "${exemptions.name}:${index + 1}"
            when {
                !line.contains('#') || line.substringAfter('#').isBlank() -> problems += "$where: $id needs a reason (`$id # <reason>`)"
                id !in items -> problems += "$where: $id is not an item of $namespace"
                id in crafted -> problems += "$where: $id has a recipe; remove the exemption"
            }
            exempt += id
        }
    }

    (items - crafted - exempt).forEach {
        problems += "$namespace:$it has no recipe (add one, or list it in ${exemptions.name} with the reason)"
    }
    results.filterValues { it.startsWith("$namespace:") && it.substringAfter(':') !in items }.forEach { (file, id) ->
        problems += "recipe $file makes $id, which is not an item of $namespace"
    }
    return problems
}
