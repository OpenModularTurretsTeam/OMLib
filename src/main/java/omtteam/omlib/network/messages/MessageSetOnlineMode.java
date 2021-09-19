package omtteam.omlib.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.util.GeneralUtil;

public class MessageSetOnlineMode implements IMessage {
    private boolean isOnline;
    private boolean isPlayerOP;

    public MessageSetOnlineMode() {
    }

    public MessageSetOnlineMode(boolean isOnline, boolean isPlayerOP) {
        this.isOnline = isOnline;
        this.isPlayerOP = isPlayerOP;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isOnline = buf.readBoolean();
        this.isPlayerOP = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isOnline);
        buf.writeBoolean(this.isPlayerOP);
    }

    public static class MessageHandlerSetOnlineMode implements IMessageHandler<MessageSetOnlineMode, IMessage> {
        @Override
        public IMessage onMessage(MessageSetOnlineMode messageIn, MessageContext ctxIn) {
            GeneralUtil.onlineMode = messageIn.isOnline;
            GeneralUtil.clientsidePlayerOP = messageIn.isPlayerOP;
            return null;
        }
    }
}
