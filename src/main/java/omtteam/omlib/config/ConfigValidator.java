package omtteam.omlib.config;

import net.minecraft.resources.ResourceLocation;
import omtteam.omlib.OMLib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Clamps config values into their allowed ranges and logs every correction.
 */
public final class ConfigValidator {
    private boolean changed;

    /** Whether any value had to be corrected. */
    public boolean changed() {
        return changed;
    }

    public int range(String name, int value, int min, int max) {
        if (value < min || value > max) {
            int fixed = Math.max(min, Math.min(max, value));
            warn(name, value, fixed, "[" + min + ", " + max + "]");
            return fixed;
        }
        return value;
    }

    public double range(String name, double value, double min, double max) {
        if (Double.isNaN(value) || value < min || value > max) {
            double fixed = Double.isNaN(value) ? min : Math.max(min, Math.min(max, value));
            warn(name, value, fixed, "[" + min + ", " + max + "]");
            return fixed;
        }
        return value;
    }

    /** Removes null entries and entries that are not valid resource locations. */
    public List<String> ids(String name, List<String> ids) {
        return filter(name, ids, entry -> ResourceLocation.tryParse(entry) != null);
    }

    /** Keeps the entries that are not null and pass {@code valid}; logs and counts as a correction what it drops. */
    public List<String> filter(String name, List<String> entries, Predicate<String> valid) {
        List<String> result = new ArrayList<>();
        if (entries == null) {
            changed = true;
            return result;
        }
        for (String entry : entries) {
            if (entry != null && valid.test(entry)) {
                result.add(entry);
            } else {
                changed = true;
                OMLib.LOGGER.warn("Config: removed invalid entry '{}' from {}", entry, name);
            }
        }
        return result;
    }

    private void warn(String name, Object value, Object fixed, String range) {
        changed = true;
        OMLib.LOGGER.warn("Config: {} = {} is outside {}, using {}", name, value, range, fixed);
    }
}
