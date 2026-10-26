package omtteam.omlib.client;

import omtteam.omlib.client.compat.ClientNetworkCompat;
import omtteam.omlib.client.screen.CamoScreen;
import omtteam.omlib.config.ConfigManager;
import omtteam.omlib.network.OMLibPackets;
import omtteam.omlib.registry.ModMenus;

/**
 * Shared client start-up of the library; the loader client entrypoints ({@code client/platform/<loader>/}) install the
 * client platform services, call this and wrap the models of the camouflage blocks.
 */
public final class OMLibClient {
    private OMLibClient() {
    }

    /** Registers the client handlers of the library's packets: the server's configs. */
    public static void init() {
        ClientNetworkCompat.registerHandler(OMLibPackets.CONFIG_SYNC,
                packet -> ConfigManager.applyServerConfig(packet.modId(), packet.json()));
    }

    /** Back to the local configs after leaving a server. */
    public static void onDisconnect() {
        ConfigManager.useLocalConfigs();
    }

    /** The screens of the library's menus, through the loader's registration. */
    public static void registerScreens(ScreenRegistrar registrar) {
        registrar.register(ModMenus.CAMO, CamoScreen::new);
    }
}
