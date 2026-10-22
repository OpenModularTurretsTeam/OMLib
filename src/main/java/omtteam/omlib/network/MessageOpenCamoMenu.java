package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.handler.OMGuiHandler;

/**
 * Client → server: open the camouflage menu of a block (the menu has a server-side container, so the server opens
 * it). The server checks distance, that camouflage is enabled and that the player may change the disguise or the
 * light.
 */
public class MessageOpenCamoMenu implements IMessage {
    private BlockPos pos;

    public MessageOpenCamoMenu() {
    }

    public MessageOpenCamoMenu(BlockPos pos) {
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

    /** Opens the menu for a player on the server, if the block is a camouflage host the player may change. */
    public static void open(EntityPlayerMP player, BlockPos pos) {
        TileEntity tileEntity = player.getEntityWorld().getTileEntity(pos);
        if (!(tileEntity instanceof ICamoSupport) || player.getDistanceSqToCenter(pos) > 64) {
            return;
        }
        ICamoSupport host = (ICamoSupport) tileEntity;
        if (host.isCamoEnabled() && (host.canSetCamo(player) || host.canChangeLight(player))) {
            player.openGui(OMLib.instance, OMGuiHandler.CAMO_GUI, player.getEntityWorld(), pos.getX(), pos.getY(),
                    pos.getZ());
        }
    }

    public static class MessageHandlerOpenCamoMenu implements IMessageHandler<MessageOpenCamoMenu, IMessage> {
        @Override
        public IMessage onMessage(MessageOpenCamoMenu message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> open(player, message.pos));
            return null;
        }
    }
}
