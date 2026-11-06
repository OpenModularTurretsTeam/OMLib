package omtteam.omlib.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import omtteam.omlib.config.mod.PrivateConfig;
import omtteam.omlib.config.mod.SampleConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The config classes of a mod are not in the library's package: sections are those of the config root's package. */
class ConfigOfAnotherPackageTest {
    @Test
    void sectionsAreWrittenAsObjects() {
        String text = ConfigWriter.write(new SampleConfig(), new SampleConfig());
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        assertEquals(5, json.getAsJsonObject("group").get("level").getAsInt(), text);
        assertTrue(text.contains("// Range: 1 to 10"), text);
    }

    @Test
    void sectionsAreClamped() {
        SampleConfig config = new SampleConfig();
        config.group.level = 99;
        ConfigValidator validator = new ConfigValidator();
        ConfigRanges.validate(config, validator, "");
        assertEquals(10, config.group.level);
        assertTrue(validator.changed());
    }

    @Test
    void privateFieldsAreWrittenAndClamped() {
        String text = ConfigWriter.write(new PrivateConfig(), new PrivateConfig());
        assertTrue(text.contains("// A private number.\n  // Range: 1 to 10\n  // Default: 5\n  \"level\": 5,"), text);
        assertTrue(text.contains("\"chance\": 0.25"), text);

        PrivateConfig config = new PrivateConfig();
        config.setLevel(99);
        config.section().setChance(3D);
        ConfigValidator validator = new ConfigValidator();
        ConfigRanges.validate(config, validator, "");
        assertEquals(10, config.level());
        assertEquals(1D, config.section().chance());
        assertTrue(validator.changed());
    }
}
