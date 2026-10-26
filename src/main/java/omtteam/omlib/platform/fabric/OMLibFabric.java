package omtteam.omlib.platform.fabric;

import net.fabricmc.api.ModInitializer;
import omtteam.omlib.platform.Platform;
import omtteam.omlib.startup.OMLibStartup;

/**
 * Fabric entrypoint of the library: installs the platform services for all mods that use it (Fabric starts a mod's
 * entrypoint after those of the mods it depends on) and registers the library's own content.
 */
public final class OMLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.ensureInstalled();
        OMLibStartup.init();
        OMLibStartup.registrationSteps().forEach(step -> step.register().run());
    }
}
