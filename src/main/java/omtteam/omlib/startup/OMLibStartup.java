package omtteam.omlib.startup;

import net.minecraft.core.registries.Registries;
import omtteam.omlib.OMLib;
import omtteam.omlib.network.OMLibPackets;
import omtteam.omlib.registry.ModMenus;
import omtteam.omlib.registry.RegistrationStep;

import java.util.List;

/**
 * Shared start-up of the library. The library's loader entrypoints ({@code platform/<loader>/}) install the platform
 * services for all mods first, call {@link #init} and run the {@link #registrationSteps()}.
 */
public final class OMLibStartup {
    private OMLibStartup() {
    }

    /** Registers the packets (NeoForge accepts packets only until its payload registration event). */
    public static void init() {
        OMLib.LOGGER.info("Initializing OMLib");
        OMLibPackets.init();
    }

    /** The registration steps in dependency order, each with the registry it fills. */
    public static List<RegistrationStep> registrationSteps() {
        return List.of(new RegistrationStep(Registries.MENU, ModMenus::init));
    }
}
