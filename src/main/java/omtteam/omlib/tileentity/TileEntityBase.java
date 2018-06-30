package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 27/11/16.
 * This Class
 */
@SuppressWarnings({"EmptyMethod", "WeakerAccess"})
@MethodsReturnNonnullByDefault
public abstract class TileEntityBase extends TileEntity {
    protected boolean updateNBT = false;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        this.writeToNBT(nbtTagCompound);
        return new SPacketUpdateTileEntity(this.pos, 2, nbtTagCompound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        IBlockState state = this.getWorld().getBlockState(pos);
        this.getWorld().notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(super.getUpdateTag());
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        markBlockForUpdate();
    }

    private void markBlockForUpdate() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().markAndNotifyBlock(this.getPos(), null, state, state, 3);
    }

    public void setUpdateNBT(boolean shouldUpdateNBT) {
        this.updateNBT = shouldUpdateNBT;
    }
}
