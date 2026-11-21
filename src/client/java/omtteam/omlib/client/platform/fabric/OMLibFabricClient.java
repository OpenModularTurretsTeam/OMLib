package omtteam.omlib.client.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import omtteam.omlib.client.OMLibClient;
import omtteam.omlib.client.camo.CamoModels;
import omtteam.omlib.client.platform.ClientPlatform;

/**
 * Fabric client entrypoint of the library: installs the client platform services for all mods that use it, registers
 * the library's screens and wraps the models of every camouflage block in the disguise model ({@code FabricCamoModel}).
 */
public final class OMLibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.ensureInstalled();
        OMLibClient.init();
        OMLibClient.registerScreens(MenuScreens::register);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> OMLibClient.onDisconnect());
        ModelLoadingPlugin.register(plugin -> {
            plugin.modifyModelAfterBake().register((model, context) -> {
                var topLevel = context.topLevelId();
                return CamoModels.isCamoModel(topLevel != null ? topLevel.id() : context.resourceId())
                        ? new FabricCamoModel(model) : model;
            });
        });
    }
}
