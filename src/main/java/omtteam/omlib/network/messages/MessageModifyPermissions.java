package omtteam.omlib.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.permission.EnumAccessLevel;
import omtteam.omlib.api.permission.GlobalTrustRegister;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.network.ISyncable;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class MessageModifyPermissions implements IMessage {
    private int x, y, z, change;
    private String player;
    private boolean hasTile;
    private boolean isGlobal;
    private Player owner;

    public MessageModifyPermissions() {
    }

    public MessageModifyPermissions(String player, @Nullable TileEntity te, boolean isGlobal, Player owner, int change) {
        if (te != null) {
            this.x = te.getPos().getX();
            this.y = te.getPos().getY();
            this.z = te.getPos().getZ();
            this.hasTile = true;
        }
        this.player = player;
        this.isGlobal = isGlobal;
        this.owner = owner;
        this.change = change;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();

        this.player = ByteBufUtils.readUTF8String(buf);
        this.owner = Player.readFromByteBuf(buf);
        this.hasTile = buf.readBoolean();
        this.isGlobal = buf.readBoolean();
        this.change = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);

        ByteBufUtils.writeUTF8String(buf, this.player);
        Player.writeToByteBuf(owner, buf);
        buf.writeBoolean(this.hasTile);
        buf.writeBoolean(this.isGlobal);
        buf.writeInt(this.change);
    }

    @SuppressWarnings("ConstantConditions")
    public static class MessageHandlerModifyPermissions implements IMessageHandler<MessageModifyPermissions, IMessage> {
        @Override
        public IMessage onMessage(MessageModifyPermissions messageIn, MessageContext ctxIn) {
            final MessageModifyPermissions message = messageIn;
            final MessageContext ctx = ctxIn;
            ((WorldServer) ctx.getServerHandler().player.getEntityWorld()).addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.getEntityWorld();
                EntityPlayerMP player = ctx.getServerHandler().player;
                if (message.hasTile) {
                    TileEntity entity = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
                    IHasTrustManager machine = null;
                    if (entity instanceof IHasTrustManager) {
                        machine = (IHasTrustManager) entity;
                    }
                    if (machine != null && PlayerUtil.isPlayerAdmin(player, machine.getTrustManager())) {
                        machine.getTrustManager().changePermission(message.player, EnumAccessLevel.values()[message.change]);
                        if (machine instanceof ISyncable)
                            ((ISyncable) machine).sendMessageToAllTracking();
                    }
                } else if (message.isGlobal) {
                    GlobalTrustRegister.instance.changePermission(message.owner, message.player, EnumAccessLevel.NONE);
                }
            });
            return null;
        }
    }
}
