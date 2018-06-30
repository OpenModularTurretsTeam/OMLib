package omtteam.omlib.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import omtteam.omlib.tileentity.ICamoSupport;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import java.util.Objects;

import static omtteam.omlib.proxy.ClientProxy.getWorld;

/**
 * Created by nico on 23/05/17.
 * This is the message used for sending an updated CamoSettings (block and light) to the client.
 */
public class MessageCamoSettings implements IMessage {
    private int x, y, z, lightValue, lightOpacity, camoBlockMeta;
    private String camoBlockRegName;

    public MessageCamoSettings() {
    }


    public MessageCamoSettings(ICamoSupport block) {
        TileEntityOwnedBlock ownedBlock = block.getOwnedBlock();
        this.x = ownedBlock.getPos().getX();
        this.y = ownedBlock.getPos().getY();
        this.z = ownedBlock.getPos().getZ();
        this.lightValue = block.getCamoSettings().getLightValue();
        this.lightOpacity = block.getCamoSettings().getLightOpacity();
        this.camoBlockRegName = Objects.requireNonNull(block.getCamoState().getBlock().getRegistryName()).toString();
        this.camoBlockMeta = block.getCamoState().getBlock().getMetaFromState(block.getCamoState());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.lightValue = buf.readInt();
        this.lightOpacity = buf.readInt();
        this.camoBlockRegName = ByteBufUtils.readUTF8String(buf);
        this.camoBlockMeta = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(lightValue);
        buf.writeInt(lightOpacity);
        ByteBufUtils.writeUTF8String(buf, camoBlockRegName);
        buf.writeInt(camoBlockMeta);
    }

    public static class MessageHandlerCamoSettings implements IMessageHandler<MessageCamoSettings, IMessage> {
        @Override
        @SuppressWarnings("deprecation")
        public IMessage onMessage(MessageCamoSettings messageIn, MessageContext ctx) {
            final MessageCamoSettings message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                TileEntity tileEntity = getWorld(FMLClientHandler.instance().getClient()).getTileEntity(new BlockPos(message.x, message.y,
                                                                                                                     message.z));
                if (tileEntity instanceof ICamoSupport) {
                    ICamoSupport base = (ICamoSupport) tileEntity;
                    base.getCamoSettings().setLightValue(message.lightValue);
                    base.getCamoSettings().setLightOpacity(message.lightOpacity);
                    base.setCamoState(Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation(message.camoBlockRegName))).getStateFromMeta(message.camoBlockMeta));
                    ((ICamoSupport) tileEntity).getOwnedBlock().markDirty();
                    base.getOwnedBlock().getWorld().checkLight(base.getOwnedBlock().getPos());
                }
            });
            return null;
        }
    }
}
