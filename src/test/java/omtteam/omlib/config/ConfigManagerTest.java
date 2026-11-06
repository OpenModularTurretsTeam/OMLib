package omtteam.omlib.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import omtteam.omlib.config.mod.SampleConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {
    @TempDir
    Path dir;

    private ConfigManager<SampleConfig> manager(String modId, List<ConfigMigration.Step> steps) {
        return new ConfigManager<>(modId, SampleConfig.class, SampleConfig::new, SampleConfig::validate, steps, () -> dir);
    }

    private Path file(String modId) {
        return dir.resolve(modId + ".json");
    }

    private JsonObject written(String modId) throws IOException {
        return JsonParser.parseString(Files.readString(file(modId))).getAsJsonObject();
    }

    @Test
    void aMissingFileIsWrittenWithDefaultsAndComments() throws IOException {
        ConfigManager<SampleConfig> manager = manager("sample_missing", List.of());
        manager.load();
        assertEquals(5, manager.get().group.level);
        assertTrue(Files.readString(file("sample_missing")).contains("// Range: 1 to 10"));
        assertEquals(5, written("sample_missing").getAsJsonObject("group").get("level").getAsInt());
    }

    @Test
    void outOfRangeUnknownAndWrongTypedValuesAreFixedAndTheFileRewritten() throws IOException {
        Files.writeString(file("sample_fix"), "{\"configVersion\": 2, \"group\": {\"level\": 99}, \"junk\": 1}");
        ConfigManager<SampleConfig> manager = manager("sample_fix", List.of());
        manager.load();
        assertEquals(10, manager.get().group.level);
        JsonObject json = written("sample_fix");
        assertFalse(json.has("junk"));
        assertEquals(10, json.getAsJsonObject("group").get("level").getAsInt());

        Files.writeString(file("sample_fix"), "{\"configVersion\": 2, \"group\": {\"level\": \"high\"}}");
        manager.load();
        assertEquals(5, manager.get().group.level, "a value of the wrong type falls back to the default");
    }

    @Test
    void oldFilesGetTheMigrationSteps() throws IOException {
        Files.writeString(file("sample_old"), "{\"configVersion\": 1, \"group\": {\"level\": 3}}");
        List<ConfigMigration.Step> steps = List.of(new ConfigMigration.Step(2, json ->
                ConfigMigration.replaceDefault(json.getAsJsonObject("group"), "level", 3, 8)));
        ConfigManager<SampleConfig> manager = manager("sample_old", steps);
        manager.load();
        assertEquals(8, manager.get().group.level);
        assertEquals(2, written("sample_old").get("configVersion").getAsInt());
    }

    @Test
    void anUpToDateFileIsNotRewritten() throws IOException {
        ConfigManager<SampleConfig> manager = manager("sample_clean", List.of());
        manager.load();
        FileTime old = FileTime.fromMillis(1_000_000_000_000L);
        Files.setLastModifiedTime(file("sample_clean"), old);
        manager.load();
        assertEquals(old, Files.getLastModifiedTime(file("sample_clean")), "nothing to fix, so the file stays as it is");
    }

    @Test
    void aFileWithoutCommentsGetsThemBack() throws IOException {
        Files.writeString(file("sample_bare"), "{\"configVersion\": 2, \"group\": {\"level\": 7}}");
        ConfigManager<SampleConfig> manager = manager("sample_bare", List.of());
        manager.load();
        assertEquals(7, manager.get().group.level);
        String text = Files.readString(file("sample_bare"));
        assertTrue(text.contains("// Range: 1 to 10"), text);
        assertEquals(7, written("sample_bare").getAsJsonObject("group").get("level").getAsInt());
    }

    @Test
    void missingKeysGetTheirDefaultAndValuesOfAnotherKindFallBack() throws IOException {
        Files.writeString(file("sample_kinds"), "{\"configVersion\": 2, \"names\": [\"x\", \"y\"]}");
        ConfigManager<SampleConfig> manager = manager("sample_kinds", List.of());
        assertEquals("sample_kinds", manager.modId());
        manager.load();
        assertEquals(List.of("x", "y"), manager.get().names, "a list stays a list");
        assertEquals(5, manager.get().group.level);
        assertEquals(5, written("sample_kinds").getAsJsonObject("group").get("level").getAsInt(), "the missing section is written");

        Files.writeString(file("sample_kinds"), "{\"configVersion\": 2, \"group\": 3, \"names\": \"x\"}");
        manager.load();
        assertEquals(5, manager.get().group.level, "a number where a section belongs falls back to the default");
        assertEquals(List.of("a"), manager.get().names, "a string where a list belongs falls back to the default");
    }

    @Test
    void theDefaultsComeFromTheSupplierNotTheConstructor() throws IOException {
        Supplier<SampleConfig> defaults = () -> {
            SampleConfig config = new SampleConfig();
            config.group.level = 7;
            return config;
        };
        ConfigManager<SampleConfig> manager = new ConfigManager<>("sample_supplied", SampleConfig.class, defaults,
                SampleConfig::validate, List.of(), () -> dir);
        Files.writeString(file("sample_supplied"), "{\"configVersion\": 2}");
        manager.load();
        assertEquals(7, manager.get().group.level, "a missing section gets the supplied default");

        Files.writeString(file("sample_supplied"), "{\"configVersion\": 2, \"group\": {\"level\": \"high\"}}");
        manager.load();
        assertEquals(7, manager.get().group.level, "a value of the wrong type gets the supplied default");
    }

    @Test
    void suppliedDefaultsAreValidatedToo() {
        ConfigManager<SampleConfig> manager = new ConfigManager<>("sample_bad_defaults", SampleConfig.class, () -> {
            SampleConfig config = new SampleConfig();
            config.group.level = 99;
            return config;
        }, SampleConfig::validate, List.of(), () -> dir);
        manager.load();
        assertEquals(10, manager.get().group.level, "an out-of-range default is clamped like a value from the file");
    }

    @Test
    void anUnreadableFileIsLeftAloneAndDefaultsAreUsed() throws IOException {
        Files.writeString(file("sample_broken"), "{ not json");
        ConfigManager<SampleConfig> manager = manager("sample_broken", List.of());
        manager.load();
        assertEquals(5, manager.get().group.level);
        assertEquals("{ not json", Files.readString(file("sample_broken")));
    }

    @Test
    void theServersConfigReplacesTheActiveOneUntilTheLocalOneIsRestored() {
        ConfigManager<SampleConfig> manager = manager("sample_sync", List.of());
        manager.load();
        manager.applyServerConfig("{\"configVersion\": 2, \"group\": {\"level\": 2}}");
        assertEquals(2, manager.get().group.level);
        assertEquals(5, manager.local().group.level);
        assertEquals(5, JsonParser.parseString(manager.localJson()).getAsJsonObject().getAsJsonObject("group").get("level").getAsInt());
        manager.applyServerConfig("not json");
        assertEquals(2, manager.get().group.level, "an invalid config from the server is ignored");
        manager.useLocalConfig();
        assertEquals(5, manager.get().group.level);
    }

    @Test
    void theServersConfigIsRoutedByModId() {
        ConfigManager<SampleConfig> first = manager("sample_a", List.of());
        ConfigManager<SampleConfig> second = manager("sample_b", List.of());
        ConfigManager.applyServerConfig("sample_b", "{\"configVersion\": 2, \"group\": {\"level\": 7}}");
        ConfigManager.applyServerConfig("sample_unknown", "{}");
        assertEquals(5, first.get().group.level);
        assertEquals(7, second.get().group.level);
        assertTrue(ConfigManager.all().contains(first) && ConfigManager.all().contains(second));
        ConfigManager.useLocalConfigs();
        assertEquals(5, second.get().group.level);
    }
}
