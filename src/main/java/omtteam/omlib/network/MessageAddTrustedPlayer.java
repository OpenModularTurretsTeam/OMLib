package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nullable;

public class MessageAddTrustedPlayer implements IMessage {
    private int x, y, z;
    private String player;
    private boolean hasTile;

    public MessageAddTrustedPlayer() {
    }

    public MessageAddTrustedPlayer(String player, @Nullable TileEntity te) {
        if (te != null) {
            this.x = te.getPos().getX();
            this.y = te.getPos().getY();
            this.z = te.getPos().getZ();
            this.hasTile = true;
        }
        this.player = player;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();

        this.player = ByteBufUtils.readUTF8String(buf);
        this.hasTile = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);

        ByteBufUtils.writeUTF8String(buf, this.player);
        buf.writeBoolean(this.hasTile);
    }

    public static class MessageHandlerAddTrustedPlayer implements IMessageHandler<MessageAddTrustedPlayer, IMessage> {
        @Override
        public IMessage onMessage(MessageAddTrustedPlayer messageIn, MessageContext ctxIn) {
            final MessageAddTrustedPlayer message = messageIn;
            PacketValidation.schedule(ctxIn, player -> {
                if (!message.hasTile) {
                    return;
                }
                PacketValidation.find(player, new BlockPos(message.x, message.y, message.z), IHasTrustManager.class)
                        .ifPresent(machine -> {
                            if (!PlayerUtil.isPlayerAdmin(player, machine.getTrustManager())) {
                                PacketValidation.sendNoPermission(player);
                                return;
                            }
                            machine.getTrustManager().addTrustedPlayer(message.player);
                            machine.informUpdate();
                        });
            });
            return null;
        }
    }
}
