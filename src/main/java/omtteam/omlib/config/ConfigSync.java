package omtteam.omlib.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import omtteam.omlib.OMLib;
import omtteam.omlib.network.MessageConfigSync;
import omtteam.omlib.network.OMLibNetworkingHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Server → client sync of Forge {@code @Config} values (1.20.1+ syncs its JSON config the same way): the values of the
 * registered mods are game rules the client needs as well (GUIs, tooltips, placement checks), so a client connected to
 * a server uses the server's values while it is connected and its own again after disconnecting.
 * <p>
 * The values are the public static fields of the mod's {@code @Config} classes plus the values registered with
 * {@link #registerValue} (config kept outside {@code @Config}), sent as JSON on login. The config files are never
 * written with the server's values: {@link #syncLocal} puts the local values back around Forge's
 * {@code ConfigManager.sync}. The single-player host is skipped, its client shares the server's fields.
 */
public final class ConfigSync {
    private static final Gson GSON = new Gson();
    /** Mod ids whose config values the server sends, with what to run after their values changed. */
    private static final Map<String, Runnable> MODS = new LinkedHashMap<>();
    /** Values outside the {@code @Config} classes, by mod id. */
    private static final Map<String, List<Value<?>>> VALUES = new HashMap<>();
    /** Client: how to put back the local values of a mod while the server's are applied. */
    private static final Map<String, List<Runnable>> LOCAL = new HashMap<>();
    /** Client: the server's values last applied for a mod. */
    private static final Map<String, String> REMOTE = new HashMap<>();

    private ConfigSync() {
    }

    /** Marks the {@code @Config} values of {@code modId} as server-authoritative (call in pre-init). */
    public static void register(String modId) {
        register(modId, () -> {
        });
    }

    /**
     * Like {@link #register(String)}; {@code onChange} runs after the mod's values changed: on the client when the
     * server's values were applied or the local ones restored, on both sides after {@link #syncLocal} (for state
     * derived from the config, such as parsed lists).
     */
    public static synchronized void register(String modId, Runnable onChange) {
        MODS.put(modId, onChange);
    }

    /**
     * Also syncs a value of {@code modId} that is not in its {@code @Config} classes (a config file of its own):
     * {@code getter} reads the current value, {@code setter} replaces it; {@code type} is its (generic) type for Gson.
     */
    public static synchronized <T> void registerValue(String modId, String key, Type type, Supplier<T> getter,
                                                      Consumer<T> setter) {
        VALUES.computeIfAbsent(modId, id -> new ArrayList<>()).add(new Value<>(key, type, getter, setter));
    }

    /** Server: sends the registered config values to a player who just logged in. */
    public static void sendTo(EntityPlayerMP player) {
        MinecraftServer server = player.getServer();
        if (server == null || (!server.isDedicatedServer() && player.getName().equals(server.getServerOwner()))) {
            return;
        }
        for (String modId : registered()) {
            OMLibNetworkingHandler.sendMessageToPlayer(new MessageConfigSync(modId, serialize(modId)), player);
        }
    }

    /** Client: applies the server's values of a mod (keeping the local ones for {@link #restoreLocal}). */
    public static synchronized void apply(String modId, String json) {
        if (!MODS.containsKey(modId)) {
            return;
        }
        applyValues(modId, json);
        MODS.get(modId).run();
    }

    private static void applyValues(String modId, String json) {
        JsonObject values = new JsonParser().parse(json).getAsJsonObject();
        if (!LOCAL.containsKey(modId)) {
            List<Runnable> restore = new ArrayList<>();
            for (Field field : syncedFields(modId)) {
                Object local = get(field);
                restore.add(() -> set(field, local));
            }
            for (Value<?> value : values(modId)) {
                restore.add(value.restorer());
            }
            LOCAL.put(modId, restore);
        }
        for (Field field : syncedFields(modId)) {
            JsonElement value = values.get(key(field));
            if (value != null) {
                set(field, GSON.fromJson(value, field.getType()));
            }
        }
        for (Value<?> value : values(modId)) {
            JsonElement element = values.get(value.jsonKey());
            if (element != null) {
                value.apply(element);
            }
        }
        REMOTE.put(modId, json);
    }

    /** Client: back to the local values of all mods (on disconnect). */
    public static synchronized void restoreLocal() {
        List<String> changed = new ArrayList<>(LOCAL.keySet());
        for (List<Runnable> restore : LOCAL.values()) {
            restore.forEach(Runnable::run);
        }
        LOCAL.clear();
        REMOTE.clear();
        for (String modId : changed) {
            MODS.getOrDefault(modId, () -> {
            }).run();
        }
    }

    /**
     * Forge's {@code ConfigManager.sync} for a changed config (the config GUI's {@code OnConfigChangedEvent}), with the
     * local values in place, so the server's values never end up in the local file; they are applied again after.
     */
    public static synchronized void syncLocal(String modId) {
        String remote = REMOTE.remove(modId);
        List<Runnable> restore = LOCAL.remove(modId);
        if (restore != null) {
            restore.forEach(Runnable::run);
        }
        ConfigManager.sync(modId, Config.Type.INSTANCE);
        if (remote != null) {
            applyValues(modId, remote);
        }
        MODS.getOrDefault(modId, () -> {
        }).run();
    }

    private static synchronized List<String> registered() {
        return new ArrayList<>(MODS.keySet());
    }

    private static synchronized List<Value<?>> values(String modId) {
        return new ArrayList<>(VALUES.getOrDefault(modId, new ArrayList<>()));
    }

    static String serialize(String modId) {
        JsonObject values = new JsonObject();
        for (Field field : syncedFields(modId)) {
            values.add(key(field), GSON.toJsonTree(get(field), field.getType()));
        }
        for (Value<?> value : values(modId)) {
            values.add(value.jsonKey(), value.toJson());
        }
        return GSON.toJson(values);
    }

    /** The public static, non-final fields of the mod's {@code @Config} classes that Forge syncs. */
    private static List<Field> syncedFields(String modId) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> configClass : ConfigManager.getModConfigClasses(modId)) {
            for (Field field : configClass.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)
                        && !field.isAnnotationPresent(Config.Ignore.class)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private static String key(Field field) {
        return field.getDeclaringClass().getName() + "#" + field.getName();
    }

    private static Object get(Field field) {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Config field " + key(field) + " is not accessible", e);
        }
    }

    private static void set(Field field, Object value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            OMLib.getLogger().error("Could not set config field {}", key(field), e);
        }
    }

    /** A value registered with {@link #registerValue}. */
    private static final class Value<T> {
        private final String key;
        private final Type type;
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        Value(String key, Type type, Supplier<T> getter, Consumer<T> setter) {
            this.key = key;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }

        /** Its key in the JSON (not a field key, which contains a '#'). */
        String jsonKey() {
            return "value:" + key;
        }

        JsonElement toJson() {
            return GSON.toJsonTree(getter.get(), type);
        }

        void apply(JsonElement element) {
            setter.accept(GSON.fromJson(element, type));
        }

        Runnable restorer() {
            T local = getter.get();
            return () -> setter.accept(local);
        }
    }
}
