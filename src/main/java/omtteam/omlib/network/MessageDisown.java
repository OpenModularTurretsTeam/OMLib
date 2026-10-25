package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.permission.IOwnedHost;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.util.player.PlayerUtil;

/**
 * Client → server: the owner gives up a turret base or camouflage trap (1.20.1+ {@code TrustPackets.Disown}). Only the
 * owner may; afterwards the block has neither owner nor trust list, and open menus of it close themselves (players
 * lose their access).
 */
public class MessageDisown implements IMessage {
    private BlockPos pos;

    public MessageDisown() {
    }

    public MessageDisown(BlockPos pos) {
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

    static void handle(MessageDisown message, EntityPlayerMP player) {
        PacketValidation.find(player, message.pos, IHasTrustManager.class).ifPresent(block -> {
            if (!(block instanceof IOwnedHost)) return;
            if (!block.getTrustManager().core().canPerformOwnerAction(PlayerUtil.getPlayerInfo(player))) {
                PacketValidation.sendNoPermission(player);
                return;
            }
            ((IOwnedHost) block).clearOwner();
            player.sendStatusMessage(new TextComponentTranslation("text.omlib.disowned"), true);
        });
    }

    public static class MessageHandlerDisown implements IMessageHandler<MessageDisown, IMessage> {
        @Override
        public IMessage onMessage(MessageDisown message, MessageContext ctx) {
            PacketValidation.schedule(ctx, player -> handle(message, player));
            return null;
        }
    }
}
