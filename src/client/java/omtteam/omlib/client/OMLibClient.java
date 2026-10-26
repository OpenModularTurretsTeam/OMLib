package omtteam.omlib.client;

import omtteam.omlib.client.screen.CamoScreen;
import omtteam.omlib.registry.ModMenus;

/**
 * Shared client start-up of the library; the loader client entrypoints ({@code client/platform/<loader>/}) install the
 * client platform services, call this and wrap the models of the camouflage blocks.
 */
public final class OMLibClient {
    private OMLibClient() {
    }

    /** The screens of the library's menus, through the loader's registration. */
    public static void registerScreens(ScreenRegistrar registrar) {
        registrar.register(ModMenus.CAMO, CamoScreen::new);
    }
}
