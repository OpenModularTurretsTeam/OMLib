package omtteam.omlib;

import net.minecraft.resources.ResourceLocation;
import omtteam.omlib.compat.VersionCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OMLib, the library of the Open Modular mods: constants of the library itself (the mods have their own).
 */
public final class OMLib {
    public static final String MOD_ID = "omlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private OMLib() {
    }

    /** An id in the library's namespace (its menu type, its packets, ...). */
    public static ResourceLocation id(String path) {
        return VersionCompat.id(MOD_ID, path);
    }
}
