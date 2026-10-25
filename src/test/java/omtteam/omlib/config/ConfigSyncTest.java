package omtteam.omlib.config;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The values registered with {@link ConfigSync#registerValue} (the mod has no {@code @Config} class here, so only
 * they are synced); {@code syncLocal} needs Forge's loader and is checked in game.
 */
class ConfigSyncTest {
    private String value;

    @Test
    void serverValuesApplyUntilRestoredAndRunOnChange() {
        AtomicInteger changes = new AtomicInteger();
        ConfigSync.register("configsynctest", changes::incrementAndGet);
        ConfigSync.registerValue("configsynctest", "value", String.class, () -> value, v -> value = v);

        value = "server";
        String json = ConfigSync.serialize("configsynctest");
        value = "local";

        ConfigSync.apply("configsynctest", json);
        assertEquals("server", value);
        assertEquals(1, changes.get());

        ConfigSync.apply("configsynctest", json); // a second sync keeps the first local value
        ConfigSync.restoreLocal();
        assertEquals("local", value);
        assertEquals(3, changes.get());
    }

    @Test
    void unregisteredModsAreIgnored() {
        value = "local";
        ConfigSync.apply("not_registered", "{\"value:value\":\"server\"}");
        assertEquals("local", value);
    }
}
