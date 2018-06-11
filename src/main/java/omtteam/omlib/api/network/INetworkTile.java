package omtteam.omlib.api.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Keridos on 30/08/17.
 * This Class
 */
@SuppressWarnings("ALL")
public interface INetworkTile {
    // TODO: write this.

    /**
     * Return, if available, the current network the tile is on.
     *
     * @return the network, or null if not on any network
     */
    @Nullable
    OMLibNetwork getNetwork();

    /**
     * Set the currently connected network for the tile.
     * If set to null, refresh on next tick (aka scan for networking tiles near own position).
     *
     * @param network the network the device should be connected to.
     */
    void setNetwork(@Nullable OMLibNetwork network);

    /**
     * Return the devices name.
     *
     * @return name of device
     */
    @SuppressWarnings("SameReturnValue")
    @Nonnull
    String getDeviceName();

    /**
     * Return the position of the tile.
     *
     * @return BlockPos position
     */
    @Nonnull
    BlockPos getPosition();

    default void recursAddDevice(World world, OMLibNetwork network, BlockPos pos, @Nullable EnumFacing from) {
        if (world.isBlockLoaded(pos)) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity te = world.getTileEntity(pos.offset(facing));
                if (!facing.equals(from) && te instanceof INetworkTile & te != null) {
                    OMLibNetwork remoteNetwork = ((INetworkTile) te).getNetwork();
                    if (remoteNetwork == null) {
                        ((INetworkTile) te).setNetwork(network);
                        ((INetworkTile) te).recursAddDevice(world, network, pos.offset(facing), facing);
                    } else if (!remoteNetwork.getUuid().equals(network.getUuid())) {
                        remoteNetwork.mergeNetwork(network);
                    }
                }
            }
        }
    }

    default OMLibNetwork createNetwork(World world) {
        OMLibNetwork network = new OMLibNetwork(world);
        network.addDevice(this);
        recursAddDevice(world, network, this.getPosition(), null);
        return network;
    }
}
