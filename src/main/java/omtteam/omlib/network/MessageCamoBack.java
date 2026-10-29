package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.container.ContainerCamo;

/**
 * Client → server: the Back button of the camouflage menu (1.20.1+ {@code CamoPackets.Back}). The server checks
 * distance, that the player has this block's camouflage menu open and that the host has a parent menu, then lets the
 * host open it.
 */
public class MessageCamoBack implements IMessage {
    private BlockPos pos;

    public MessageCamoBack() {
    }

    public MessageCamoBack(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    private static void back(EntityPlayerMP player, BlockPos pos) {
        PacketValidation.find(player, pos, ICamoSupport.class).ifPresent(host -> {
            if (host.hasParentMenu() && player.openContainer instanceof ContainerCamo
                    && ((ContainerCamo) player.openContainer).getHost() == host) {
                host.openParentMenu(player);
            }
        });
    }

    public static class MessageHandlerCamoBack implements IMessageHandler<MessageCamoBack, IMessage> {
        @Override
        public IMessage onMessage(MessageCamoBack message, MessageContext ctx) {
            PacketValidation.schedule(ctx, player -> back(player, message.pos));
            return null;
        }
    }
}
