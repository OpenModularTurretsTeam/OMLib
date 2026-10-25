package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.util.camo.CamoSettings;

/**
 * Client → server: new light value and opacity from the sliders of the camouflage menu. The server checks distance,
 * that the block supports light and that the player may change it.
 */
public class MessageCamoLight implements IMessage {
    private BlockPos pos;
    private int value;
    private int opacity;

    public MessageCamoLight() {
    }

    public MessageCamoLight(BlockPos pos, int value, int opacity) {
        this.pos = pos;
        this.value = value;
        this.opacity = opacity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.value = buf.readByte();
        this.opacity = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(value);
        buf.writeByte(opacity);
    }

    /** Applies new light settings to a host and the world (light engine and clients). */
    private static void applyLight(ICamoSupport host, int value, int opacity) {
        CamoSettings settings = host.getCamoSettings();
        settings.setLightValue(CamoLight.clamp(value));
        settings.setLightOpacity(CamoLight.clamp(opacity));
        host.getCamoTileEntity().markDirty();
        host.updateCamoSettingsToPlayers();
        host.getCamoTileEntity().getWorld().checkLight(host.getCamoTileEntity().getPos());
    }

    public static class MessageHandlerCamoLight implements IMessageHandler<MessageCamoLight, IMessage> {
        @Override
        public IMessage onMessage(MessageCamoLight message, MessageContext ctx) {
            PacketValidation.schedule(ctx, player ->
                    PacketValidation.find(player, message.pos, ICamoSupport.class).ifPresent(host -> {
                        if (host.isCamoEnabled() && host.supportsLight() && host.canChangeLight(player)) {
                            applyLight(host, message.value, message.opacity);
                        }
                    }));
            return null;
        }
    }
}
