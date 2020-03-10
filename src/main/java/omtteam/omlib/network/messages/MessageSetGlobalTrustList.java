package omtteam.omlib.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.permission.GlobalTrustRegister;
import omtteam.omlib.api.permission.TrustedPlayersManager;
import omtteam.omlib.util.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nico on 23/05/17.
 */
public class MessageSetGlobalTrustList implements IMessage {
    private HashMap<Player, TrustedPlayersManager> globalTrustList;

    public MessageSetGlobalTrustList() {
    }

    public MessageSetGlobalTrustList(GlobalTrustRegister globalTrustRegister) {
        this.globalTrustList = globalTrustRegister.getGlobalTrustList();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        globalTrustList = new HashMap<>();
        int lengthofMap = buf.readInt();
        for (int i = 0; i < lengthofMap; i++) {
            Player owner = Player.readFromByteBuf(buf);
            TrustedPlayersManager tpm = new TrustedPlayersManager(owner);
            tpm.readFromByteBuf(buf);
            globalTrustList.put(owner, tpm);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(globalTrustList.size());
        for (Map.Entry<Player, TrustedPlayersManager> entry : globalTrustList.entrySet()) {
            Player owner = entry.getKey();
            Player.writeToByteBuf(owner, buf);
            entry.getValue().writeToByteBuf(buf);
        }
    }

    public static class MessageHandlerSetGlobalTrustList implements IMessageHandler<MessageSetGlobalTrustList, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageSetGlobalTrustList messageIn, MessageContext ctx) {
            final MessageSetGlobalTrustList message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> GlobalTrustRegister.instance.setGlobalTrustList(message.globalTrustList));
            return null;
        }
    }
}
