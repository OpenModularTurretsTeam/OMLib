package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.api.permission.TrustedPlayersManager;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.util.player.Player;
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
                            if (addTrustedPlayer(player, machine.getTrustManager(), message.player)) {
                                machine.informUpdate();
                            }
                        });
            });
            return null;
        }

        /**
         * Adds the named player and tells the sender why not if that fails (as on 1.20.1+): unknown name, already on
         * the list, or the owner. A player from the username cache always has a UUID, so those are the only reasons.
         */
        private static boolean addTrustedPlayer(EntityPlayerMP sender, TrustedPlayersManager trust, String name) {
            Player target = PlayerUtil.getPlayerFromUsernameCache(name);
            if (target == null) {
                sender.sendMessage(new TextComponentTranslation("text.omlib.player_not_found", name));
                return false;
            }
            if (trust.addTrustedPlayer(new TrustedPlayer(target))) {
                return true;
            }
            if (trust.getTrustedPlayer(target.getUuid()) != null || trust.getTrustedPlayer(target.getName()) != null) {
                sender.sendMessage(new TextComponentTranslation("text.omlib.already_trusted", target.getName()));
            } else {
                sender.sendMessage(new TextComponentTranslation("text.omlib.owner_not_trustable"));
            }
            return false;
        }
    }
}
