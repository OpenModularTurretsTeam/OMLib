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

@SuppressWarnings("unused")
public class MessageModifyPermissions implements IMessage {
    private int x, y, z, change;
    private String player;
    private boolean hasTile;

    public MessageModifyPermissions() {
    }

    public MessageModifyPermissions(String player, @Nullable TileEntity te, int change) {
        if (te != null) {
            this.x = te.getPos().getX();
            this.y = te.getPos().getY();
            this.z = te.getPos().getZ();
            this.hasTile = true;
        }
        this.player = player;
        this.change = change;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();

        this.player = ByteBufUtils.readUTF8String(buf);
        this.hasTile = buf.readBoolean();
        this.change = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);

        ByteBufUtils.writeUTF8String(buf, this.player);
        buf.writeBoolean(this.hasTile);
        buf.writeInt(this.change);
    }

    @SuppressWarnings("ConstantConditions")
    public static class MessageHandlerModifyPermissions implements IMessageHandler<MessageModifyPermissions, IMessage> {
        @Override
        public IMessage onMessage(MessageModifyPermissions messageIn, MessageContext ctxIn) {
            final MessageModifyPermissions message = messageIn;
            PacketValidation.schedule(ctxIn, player -> {
                if (!message.hasTile) {
                    return;
                }
                PacketValidation.find(player, new BlockPos(message.x, message.y, message.z), IHasTrustManager.class)
                        .ifPresent(machine -> {
                            if (!PlayerUtil.isPlayerAdmin(player, machine)) {
                                PacketValidation.sendNoPermission(player);
                                return;
                            }
                            machine.getTrustManager().changePermission(message.player, message.change);
                            machine.informUpdate();
                        });
            });
            return null;
        }
    }
}
