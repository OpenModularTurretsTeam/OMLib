package omtteam.omlib.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import omtteam.omlib.config.mod.SampleConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
    void anUnreadableFileIsLeftAloneAndDefaultsAreUsed() throws IOException {
        Files.writeString(file("sample_broken"), "{ not json");
        ConfigManager<SampleConfig> manager = manager("sample_broken", List.of());
        manager.load();
        assertEquals(5, manager.get().group.level);
        assertEquals("{ not json", Files.readString(file("sample_broken")));
    }

    @Test
    void theServersConfigReplacesTheActiveOneUntilTheLocalOneIsRestored() throws IOException {
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
