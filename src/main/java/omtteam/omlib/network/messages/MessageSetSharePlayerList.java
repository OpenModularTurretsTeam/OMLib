package omtteam.omlib.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.handler.OwnerShareHandler;
import omtteam.omlib.util.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nico on 23/05/17.
 */
public class MessageSetSharePlayerList implements IMessage {
    private HashMap<Player, ArrayList<Player>> ownerShareMap;

    public MessageSetSharePlayerList() {
    }

    public MessageSetSharePlayerList(OwnerShareHandler shareHandler) {
        this.ownerShareMap = shareHandler.getOwnerShareMap();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ownerShareMap = new HashMap<>();
        int lengthofMap = buf.readInt();
        for (int i = 0; i < lengthofMap; i++) {
            Player owner = Player.readFromByteBuf(buf);
            ArrayList<Player> sharePlayerList = new ArrayList<>();
            int lengthOfPlayerList = buf.readInt();
            if (lengthOfPlayerList > 0) {
                for (int j = 0; j < lengthOfPlayerList; j++) {
                    sharePlayerList.add(Player.readFromByteBuf(buf));
                }
            }
            ownerShareMap.put(owner, sharePlayerList);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ownerShareMap.size());
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            Player owner = entry.getKey();
            Player.writeToByteBuf(owner, buf);
            buf.writeInt(entry.getValue().size());
            if (entry.getValue().size() > 0) {
                for (Player player : entry.getValue()) {
                    Player.writeToByteBuf(player, buf);
                }
            }
        }
    }

    public static class MessageHandlerSetSharePlayerList implements IMessageHandler<MessageSetSharePlayerList, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        @SuppressWarnings("deprecation")
        public IMessage onMessage(MessageSetSharePlayerList messageIn, MessageContext ctx) {
            final MessageSetSharePlayerList message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> OwnerShareHandler.getInstance().setOwnerShareMap(messageIn.ownerShareMap));
            return null;
        }
    }
}
