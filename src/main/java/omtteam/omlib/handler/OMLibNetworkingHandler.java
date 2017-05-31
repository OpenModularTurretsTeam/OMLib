package omtteam.omlib.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import omtteam.omlib.network.messages.MessageSetSharePlayerList;
import omtteam.omlib.reference.Reference;

/**
 * Created by nico on 21/05/17.
 */
public class OMLibNetworkingHandler {
    private final static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void initNetworking() {
        INSTANCE.registerMessage(MessageSetSharePlayerList.MessageHandlerSetSharePlayerList.class, MessageSetSharePlayerList.class, 0,
                Side.CLIENT);
    }

    public static void sendMessageToAllPlayers(IMessage message) {
        INSTANCE.sendToAll(message);
    }

    public static void sendMessageToPlayer(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }
}