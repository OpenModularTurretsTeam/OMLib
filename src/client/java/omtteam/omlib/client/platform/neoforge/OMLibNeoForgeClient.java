package omtteam.omlib.client.platform.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import omtteam.omlib.OMLib;
import omtteam.omlib.client.OMLibClient;
import omtteam.omlib.client.camo.CamoModels;
import omtteam.omlib.client.platform.ClientPlatform;
//? if >=1.21.11 {
/*import omtteam.omlib.camo.CamoBlock;
*///?}

/**
 * NeoForge client entrypoint of the library: installs the client platform services for all mods that use it, registers
 * the library's screens and wraps the models of every camouflage block in the disguise model ({@code NeoForgeCamoModel}).
 * FML constructs it after the common {@code OMLibNeoForge}, so {@link Platform} is already there.
 */
@Mod(value = OMLib.MOD_ID, dist = Dist.CLIENT)
public final class OMLibNeoForgeClient {
    public OMLibNeoForgeClient(IEventBus modBus) {
        ClientPlatform.ensureInstalled();
        OMLibClient.init();
        modBus.addListener((RegisterMenuScreensEvent event) -> OMLibClient.registerScreens(event::register));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> OMLibClient.onDisconnect());
        //? if >=1.21.11 {
        /*modBus.addListener((ModelEvent.ModifyBakingResult event) -> event.getBakingResult().blockStateModels()
                .replaceAll((state, model) -> state.getBlock() instanceof CamoBlock ? new NeoForgeCamoModel(model) : model));
        *///?} else {
        modBus.addListener((ModelEvent.ModifyBakingResult event) -> event.getModels().replaceAll((id, model) ->
                CamoModels.isCamoModel(id.id()) ? new NeoForgeCamoModel(model) : model));
        //?}
    }
}
