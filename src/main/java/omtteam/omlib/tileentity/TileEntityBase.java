package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 27/11/16.
 * The 3.x tile entity base of the OMT mods on top of {@link TileEntitySynced}: subclasses save and load through
 * {@link #saveToNBT} / {@link #loadFromNBT} (calling super first), clients get the full saved data, and every update
 * packet also re-renders the block (camouflage, light settings).
 */
@SuppressWarnings({"EmptyMethod", "WeakerAccess"})
@MethodsReturnNonnullByDefault
public abstract class TileEntityBase extends TileEntitySynced {
    protected boolean updateNBT = false;

    @Override
    protected void readData(NBTTagCompound tag) {
        loadFromNBT(tag);
    }

    @Override
    protected void writeData(NBTTagCompound tag) {
        saveToNBT(tag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        IBlockState state = this.getWorld().getBlockState(pos);
        this.getWorld().notifyBlockUpdate(pos, state, state, 3);
    }

    public void markBlockForUpdate() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().markAndNotifyBlock(this.getPos(), null, state, state, 3);
    }

    public void setUpdateNBT(boolean shouldUpdateNBT) {
        this.updateNBT = shouldUpdateNBT;
    }

    // Abstraction Functions

    /** Saves the data of this tile entity; position and id are written by {@link TileEntitySynced} before. */
    @ParametersAreNonnullByDefault
    public NBTTagCompound saveToNBT(@Nonnull NBTTagCompound tag) {
        return tag;
    }

    /** Loads what {@link #saveToNBT} saved; the position is read by {@link TileEntitySynced} before. */
    @ParametersAreNonnullByDefault
    public void loadFromNBT(@Nonnull NBTTagCompound tag) {
    }
}
