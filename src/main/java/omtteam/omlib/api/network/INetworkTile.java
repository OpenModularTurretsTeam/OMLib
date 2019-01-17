package omtteam.omlib.api.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.handler.OMLibEventHandler;
import omtteam.omlib.util.WorldUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keridos on 30/08/17.
 * This Class
 */
public interface INetworkTile {
    // TODO: write this.

    /**
     * Return, if available, the current network the tile is on.
     *
     * @return the network
     */
    OMLibNetwork getNetwork();

    /**
     * Set the currently connected network for the tile.
     *
     * @param network the network the device should be connected to.
     */
    void setNetwork(OMLibNetwork network);

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
                    if (remoteNetwork != null && !remoteNetwork.getUuid().equals(network.getUuid())) {
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
        OMLibEventHandler.getInstance().registerNetwork(network);
        return network;
    }

    /**
     * Call this when building networks (f.ex. on world load)
     *
     * @param world       the world the device is in.
     * @param pos         the position of the device.
     * @param networkName the name of the network (only for controller)
     */
    default void scan(World world, BlockPos pos, String networkName) {
        List<INetworkTile> list = this.getTouchingNetworkTiles(world, pos);
        List<OMLibNetwork> networks = new ArrayList<>();
        OMLibNetwork network;
        for (INetworkTile tile : list) {
            if (tile.getNetwork() != null && !networks.contains(tile.getNetwork())) {
                networks.add(tile.getNetwork());
            }
        }
        if (networks.isEmpty()) {
            network = this.createNetwork(world);
            network.addDevice(this);
            this.setNetwork(network);
        } else if (networks.size() == 1) {
            network = networks.get(0);
            if (network.addDevice(this)) {
                this.setNetwork(network);
            }
        } else {
            network = networks.get(0);
            for (int i = 1; i < networks.size(); i++) {
                network.mergeNetwork(networks.get(i));
                if (network.addDevice(this)) {
                    this.setNetwork(network);
                }
            }
        }
        if (!networkName.isEmpty()) {
            network.setName(networkName);
        }
    }

    /**
     * Return a list of all networking capable tiles near the cable.
     *
     * @return the list.
     */
    default List<INetworkTile> getTouchingNetworkTiles(World world, BlockPos pos) {
        return WorldUtil.getTouchingTileEntitiesByClass(world, pos, INetworkTile.class);
    }
}
