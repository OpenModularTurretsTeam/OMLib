package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import omtteam.omlib.api.render.camo.ICamoSupport;

import java.util.Objects;

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
        TileEntity host = block.getCamoTileEntity();
        this.x = host.getPos().getX();
        this.y = host.getPos().getY();
        this.z = host.getPos().getZ();
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

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    public int getLightValue() {
        return lightValue;
    }

    public int getLightOpacity() {
        return lightOpacity;
    }

    public String getCamoBlockRegName() {
        return camoBlockRegName;
    }

    public int getCamoBlockMeta() {
        return camoBlockMeta;
    }
}
