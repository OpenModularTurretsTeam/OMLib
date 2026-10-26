package omtteam.omlib.client.platform.forge;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import omtteam.omlib.client.OMLibClient;
import omtteam.omlib.client.camo.CamoModels;
import omtteam.omlib.client.platform.ClientPlatform;

/**
 * Forge client entrypoint of the library: installs the client platform services for all mods that use it, registers
 * the library's screens and wraps the models of every camouflage block in the disguise model ({@code ForgeCamoModel}).
 * Forge 1.20.1 has no client-only {@code @Mod} classes: the common {@code OMLibForge} constructs this one by name on a
 * client, after installing {@link Platform}.
 */
public final class OMLibForgeClient {
    public OMLibForgeClient(IEventBus modBus) {
        ClientPlatform.ensureInstalled();
        OMLibClient.init();
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> OMLibClient.onDisconnect());
        // no screen event on Forge 1.20.1: on the main thread during client setup
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                OMLibClient.registerScreens(MenuScreens::register)));
        modBus.addListener((ModelEvent.ModifyBakingResult event) -> event.getModels().replaceAll((id, model) ->
                CamoModels.isCamoModel(id) ? new ForgeCamoModel(model) : model));
    }
}
