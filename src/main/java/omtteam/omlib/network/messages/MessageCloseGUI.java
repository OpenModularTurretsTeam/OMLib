package omtteam.omlib.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.network.ISyncable;

/**
 * Created by nico on 23/05/17.
 */
public class MessageCloseGUI implements IMessage {
    private int x, y, z;


    public MessageCloseGUI() {
    }


    public static class MessageHandlerCloseGUI implements IMessageHandler<MessageCloseGUI, IMessage> {
        @Override
        @SuppressWarnings("deprecation")
        public IMessage onMessage(MessageCloseGUI messageIn, MessageContext ctx) {
            final MessageCloseGUI message = messageIn;
            final EntityPlayerMP player = ctx.getServerHandler().player;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = player.world;
                TileEntity te = world.getTileEntity(new BlockPos(message.x, message.y, message.z));
                if (te instanceof ISyncable) {
                    ((ISyncable) te).getSyncPlayerList().remove(player);
                }
            });
            return null;
        }

    }

    public MessageCloseGUI(TileEntity te) {
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
}
