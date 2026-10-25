package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.network.ISyncableTE;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.util.player.PlayerUtil;

import java.util.List;

/**
 * Created by nico on 23/05/17.
 */
public class MessageOpenGUITile implements IMessage {
    private int x, y, z;

    public MessageOpenGUITile() {
    }

    public MessageOpenGUITile(TileEntity te) {
        this.x = te.getPos().getX();
        this.y = te.getPos().getY();
        this.z = te.getPos().getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /** Only players who may open the block's GUI get its updates. */
    private static boolean mayWatch(EntityPlayerMP player, ISyncableTE te) {
        return !(te instanceof IHasOwner) || PlayerUtil.canPlayerAccessBlock(player, (IHasOwner) te);
    }

    public static class MessageHandlerOpenGUITile implements IMessageHandler<MessageOpenGUITile, IMessage> {
        @Override
        public IMessage onMessage(MessageOpenGUITile messageIn, MessageContext ctx) {
            final MessageOpenGUITile message = messageIn;
            PacketValidation.schedule(ctx, player ->
                    PacketValidation.find(player, new BlockPos(message.x, message.y, message.z), ISyncableTE.class)
                            .ifPresent(te -> {
                                List<EntityPlayerMP> players = te.getSyncPlayerList();
                                if (mayWatch(player, te) && !players.contains(player)) {
                                    players.add(player);
                                }
                            }));
            return null;
        }
    }
}
