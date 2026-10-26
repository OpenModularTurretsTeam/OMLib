package omtteam.omlib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import omtteam.omlib.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Loads, validates, migrates and saves {@code config/<mod id>.json} of a mod, and holds the server's config on clients
 * (the library sends it when a player joins, see {@code ConfigSync}).
 * <p>
 * The config is a plain object with {@link Comment}s and {@link Range}s (see {@link ConfigWriter}). Missing keys are
 * filled in with defaults, unknown keys are dropped, values of the wrong JSON type are replaced and out-of-range
 * values are clamped; the file is rewritten whenever that happens. The file is JSON with {@code //} comments (read
 * leniently). It is also rewritten when it differs from what the mod would write, which refreshes the comments after an
 * update; comments added by users are not kept. Older files are updated by the {@link ConfigMigration.Step}s.
 * <p>
 * A mod creates one manager (usually behind a small facade class of its own) and calls {@link #load} when it starts.
 *
 * @param <T> the config class: a public class whose fields are values or sections (classes in its own package)
 */
public final class ConfigManager<T> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    /** All managers by mod id: the library syncs their configs to joining clients. */
    private static final Map<String, ConfigManager<?>> MANAGERS = new ConcurrentHashMap<>();

    private final String modId;
    private final Logger logger;
    private final Class<T> type;
    private final Supplier<T> defaults;
    private final BiConsumer<T, ConfigValidator> validator;
    private final List<ConfigMigration.Step> steps;
    private final Supplier<Path> directory;

    /** Config loaded from this instance's own file. */
    private T local;
    /** Config in effect: the server's while connected to a remote server, otherwise {@link #local}. */
    private volatile T active;

    /**
     * @param modId     the mod id; the file is {@code <config folder>/<modId>.json}
     * @param type      the config class
     * @param defaults  creates a config object with the default values
     * @param validator clamps and cleans a config object (usually {@code ConfigRanges.validate(config, v, "")} plus
     *                  the mod's own checks), reporting corrections to the {@link ConfigValidator}
     * @param steps     the migrations of older files, see {@link ConfigMigration}
     */
    public ConfigManager(String modId, Class<T> type, Supplier<T> defaults, BiConsumer<T, ConfigValidator> validator,
                         List<ConfigMigration.Step> steps) {
        this(modId, type, defaults, validator, steps, () -> Platform.loaderInfo().configDir());
    }

    /** With another folder for the file, for tests. */
    ConfigManager(String modId, Class<T> type, Supplier<T> defaults, BiConsumer<T, ConfigValidator> validator,
                  List<ConfigMigration.Step> steps, Supplier<Path> directory) {
        this.modId = modId;
        this.logger = LoggerFactory.getLogger(modId);
        this.type = type;
        this.defaults = defaults;
        this.validator = validator;
        this.steps = steps;
        this.directory = directory;
        // plain defaults until load(): a mod's validator may depend on state that is set up after this constructor
        this.local = defaults.get();
        this.active = local;
        MANAGERS.put(modId, this);
    }

    /** The config currently in effect. Do not modify the returned object. */
    public T get() {
        return active;
    }

    /**
     * The config loaded from this instance's own file, also while connected to a remote server. Only for client
     * settings that each player chooses; gameplay values come from {@link #get()}. Do not modify it.
     */
    public T local() {
        return local;
    }

    /** The config loaded from this instance's own file as JSON, which the server sends to joining clients. */
    public String localJson() {
        return GSON.toJson(local);
    }

    /** Loads the config file (writing defaults if it is missing). */
    public void load() {
        Path path = directory.get().resolve(modId + ".json");
        T data;
        boolean changed;
        if (Files.exists(path)) {
            JsonElement json;
            try (Reader reader = Files.newBufferedReader(path)) {
                json = JsonParser.parseReader(reader);
            } catch (Exception e) {
                logger.error("Could not read {}, using defaults (the file is left untouched)", path, e);
                local = active = validatedDefaults();
                return;
            }
            ParseResult<T> result = parse(json);
            data = result.data();
            changed = result.changed() || !commented(data).equals(readText(path));
        } else {
            data = validatedDefaults();
            changed = true;
        }
        if (changed) {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, commented(data));
            } catch (IOException e) {
                logger.error("Could not write {}", path, e);
            }
        }
        local = active = data;
    }

    /** Client side: use the config received from the server. */
    public void applyServerConfig(String json) {
        try {
            active = parse(JsonParser.parseString(json)).data();
        } catch (Exception e) {
            logger.error("Received an invalid config from the server, keeping the local one", e);
        }
    }

    /** Client side: go back to the local config, e.g. after disconnecting. */
    public void useLocalConfig() {
        active = local;
    }

    /** The managers of all mods, for syncing their configs. */
    public static List<ConfigManager<?>> all() {
        return List.copyOf(MANAGERS.values());
    }

    /** The mod id of this manager. */
    public String modId() {
        return modId;
    }

    /** Client side: the server's config for the mod with this id; ignored if no manager has that id. */
    public static void applyServerConfig(String modId, String json) {
        ConfigManager<?> manager = MANAGERS.get(modId);
        if (manager != null) manager.applyServerConfig(json);
    }

    /** Client side: all mods go back to their local config. */
    public static void useLocalConfigs() {
        MANAGERS.values().forEach(ConfigManager::useLocalConfig);
    }

    /** The defaults, validated like a loaded file (lists parsed, ranges applied). */
    private T validatedDefaults() {
        T data = defaults.get();
        validator.accept(data, new ConfigValidator());
        return data;
    }

    /** The file contents for {@code data}: JSON with the comments from {@link Comment} and {@link Range}. */
    private String commented(T data) {
        return ConfigWriter.write(data, defaults.get());
    }

    private static String readText(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
    }

    private record ParseResult<T>(T data, boolean changed) {
    }

    private ParseResult<T> parse(JsonElement json) {
        JsonObject defaultTree = GSON.toJsonTree(defaults.get()).getAsJsonObject();
        boolean[] changed = {false};
        // before merging: a file without a version gets the current one from the defaults
        if (json.isJsonObject()) changed[0] = ConfigMigration.migrate(json.getAsJsonObject(), steps);
        JsonElement merged = json.isJsonObject() ? merge(defaultTree, json.getAsJsonObject(), "", changed) : defaultTree;
        if (!json.isJsonObject()) {
            logger.warn("Config: root is not a JSON object, using defaults");
            changed[0] = true;
        }
        T data = GSON.fromJson(merged, type);
        ConfigValidator configValidator = new ConfigValidator();
        validator.accept(data, configValidator);
        return new ParseResult<>(data, changed[0] || configValidator.changed());
    }

    /**
     * Overlays {@code user} onto {@code defaults}: keeps user values whose JSON type matches the default,
     * falls back to the default otherwise and drops unknown keys.
     */
    private JsonObject merge(JsonObject defaultTree, JsonObject user, String path, boolean[] changed) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : defaultTree.entrySet()) {
            String key = entry.getKey();
            JsonElement def = entry.getValue();
            JsonElement value = user.get(key);
            if (value == null) {
                changed[0] = true;
                result.add(key, def);
            } else if (def.isJsonObject() && value.isJsonObject()) {
                result.add(key, merge(def.getAsJsonObject(), value.getAsJsonObject(), path + key + ".", changed));
            } else if (sameType(def, value)) {
                result.add(key, value);
            } else {
                logger.warn("Config: {}{} has the wrong type, using the default {}", path, key, def);
                changed[0] = true;
                result.add(key, def);
            }
        }
        for (String key : user.keySet()) {
            if (!defaultTree.has(key)) {
                logger.warn("Config: removed unknown key {}{}", path, key);
                changed[0] = true;
            }
        }
        return result;
    }

    private static boolean sameType(JsonElement a, JsonElement b) {
        if (a.isJsonArray()) return b.isJsonArray();
        if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
            JsonPrimitive pa = a.getAsJsonPrimitive();
            JsonPrimitive pb = b.getAsJsonPrimitive();
            return pa.isBoolean() == pb.isBoolean() && pa.isNumber() == pb.isNumber() && pa.isString() == pb.isString();
        }
        return false;
    }
}
