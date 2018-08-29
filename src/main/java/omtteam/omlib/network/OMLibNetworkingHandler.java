package omtteam.omlib.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import omtteam.omlib.network.messages.MessageCamoSettings;
import omtteam.omlib.network.messages.MessageCloseGUI;
import omtteam.omlib.network.messages.MessageOpenGUI;
import omtteam.omlib.network.messages.MessageSetSharePlayerList;
import omtteam.omlib.network.messages.render.MessageRenderRay;
import omtteam.omlib.reference.Reference;

/**
 * Created by nico on 21/05/17.
 */
public class OMLibNetworkingHandler {
    public final static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void initNetworking() {
        INSTANCE.registerMessage(MessageSetSharePlayerList.MessageHandlerSetSharePlayerList.class,
                                 MessageSetSharePlayerList.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(MessageOpenGUI.MessageHandlerOpenGUI.class, MessageOpenGUI.class,
                                 1, Side.SERVER);
        INSTANCE.registerMessage(MessageCloseGUI.MessageHandlerCloseGUI.class, MessageCloseGUI.class,
                                 2, Side.SERVER);
        INSTANCE.registerMessage(MessageCamoSettings.MessageHandlerCamoSettings.class, MessageCamoSettings.class,
                                 3, Side.CLIENT);
        INSTANCE.registerMessage(MessageRenderRay.MessageHandlerRenderRay.class, MessageRenderRay.class,
                                 4, Side.CLIENT);
    }

    public static void sendMessageToAllPlayers(IMessage message) {
        INSTANCE.sendToAll(message);
    }

    public static void sendMessageToPlayer(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }
}