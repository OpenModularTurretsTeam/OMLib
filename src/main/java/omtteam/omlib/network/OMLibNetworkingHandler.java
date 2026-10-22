package omtteam.omlib.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import omtteam.omlib.OMLib;
import omtteam.omlib.reference.Reference;

/**
 * Created by nico on 21/05/17.
 */
public class OMLibNetworkingHandler {
    public final static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void initNetworking() {
        INSTANCE.registerMessage(OMLib.proxy.clientMessageHandler(MessageSetSharePlayerList.class),
                                 MessageSetSharePlayerList.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(MessageOpenGUITile.MessageHandlerOpenGUITile.class, MessageOpenGUITile.class,
                                 1, Side.SERVER);
        INSTANCE.registerMessage(MessageCloseGUITile.MessageHandlerCloseGUITile.class, MessageCloseGUITile.class,
                                 2, Side.SERVER);
        INSTANCE.registerMessage(OMLib.proxy.clientMessageHandler(MessageCamoSettings.class), MessageCamoSettings.class,
                                 3, Side.CLIENT);
        INSTANCE.registerMessage(OMLib.proxy.clientMessageHandler(MessageRenderRay.class), MessageRenderRay.class,
                                 4, Side.CLIENT);
        // 5 was MessageSetGlobalTrustList (global trust lists, removed in 4.0.0)

        INSTANCE.registerMessage(MessageModifyPermissions.MessageHandlerModifyPermissions.class,
                                 MessageModifyPermissions.class, 6, Side.SERVER);

        INSTANCE.registerMessage(MessageAddTrustedPlayer.MessageHandlerAddTrustedPlayer.class,
                                 MessageAddTrustedPlayer.class, 7, Side.SERVER);

        INSTANCE.registerMessage(MessageRemoveTrustedPlayer.MessageHandlerRemoveTrustedPlayer.class,
                                 MessageRemoveTrustedPlayer.class, 8, Side.SERVER);
        INSTANCE.registerMessage(OMLib.proxy.clientMessageHandler(MessageSpawnParticleQuad.class),
                                 MessageSpawnParticleQuad.class, 9, Side.CLIENT);
        INSTANCE.registerMessage(OMLib.proxy.clientMessageHandler(MessageRenderLightning.class),
                                 MessageRenderLightning.class, 10, Side.CLIENT);
    }

    public static void sendMessageToAllPlayers(IMessage message) {
        INSTANCE.sendToAll(message);
    }

    public static void sendMessageToPlayer(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }
}