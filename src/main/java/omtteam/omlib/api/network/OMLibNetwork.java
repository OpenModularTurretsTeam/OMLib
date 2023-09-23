package omtteam.omlib.api.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.handler.OMLibEventHandler;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.UUID.randomUUID;

/**
 * Created by Keridos on 30/08/17.
 * This Class
 */
public class OMLibNetwork {
    private Map<BlockPos, INetworkTile> devices = new HashMap<>();
    private World world;
    private final UUID uuid;
    private String name;
    private Player owner;

    /**
     * This is the default constructor for a network without a custom name.
     * Names can be added to an existing network later.
     *
     * @param world the world the network is in
     */
    public OMLibNetwork(World world) {
        this.world = world;
        this.uuid = randomUUID();
        OMLibEventHandler.getInstance().registerNetwork(this);
    }

    /**
     * This is the constructor for a network with a give name.
     *
     * @param world the world the network is in
     * @param name  the name of the network
     */
    public OMLibNetwork(World world, String name) {
        this.world = world;
        this.uuid = randomUUID();
        this.name = name;
        OMLibEventHandler.getInstance().registerNetwork(this);
    }

    /**
     * This is the method that searches for energy giving and energy requiring devices and
     * gathers and distributes the energy in the network
     * Do NOT call this manually anywhere.
     */
    public void tick() {
        List<IPowerExchangeTile> delivering = new ArrayList<>();
        List<IPowerExchangeTile> requiring = new ArrayList<>();
        int powerRequired = 0, powerToDeliver = 0;

        for (Map.Entry<BlockPos, INetworkTile> device : devices.entrySet()) {
            if (world.isBlockLoaded(device.getKey()) && device.getValue() instanceof IPowerExchangeTile) {
                if (((IPowerExchangeTile) device.getValue()).deliversEnergy()) {
                    delivering.add((IPowerExchangeTile) device.getValue());
                } else if (((IPowerExchangeTile) device.getValue()).requiresEnergy()) {
                    requiring.add((IPowerExchangeTile) device.getValue());
                }
            }
        }
        for (IPowerExchangeTile tile : requiring) {
            powerRequired += Math.min(tile.getEnergyStorage().getMaxReceive(),
                                      tile.getEnergyStorage().getMaxEnergyStored() - tile.getEnergyStorage().getEnergyStored());
        }
        for (IPowerExchangeTile tile : delivering) {
            powerToDeliver += Math.min(powerRequired - powerToDeliver, Math.min(tile.getEnergyStorage().getMaxExtract(),
                                                                                tile.getEnergyStorage().getMaxEnergyStored() - tile.getEnergyStorage().getEnergyStored()));
        }
        for (IPowerExchangeTile tile : requiring) {
            tile.getEnergyStorage().receiveEnergy(powerToDeliver / requiring.size(), false);
            powerToDeliver -= powerToDeliver / requiring.size();
        }
    }

    /**
     * This returns, if existing theController in the network.
     */
    @Nullable
    public <T> T getController(Class<T> clazz) {
        for (INetworkTile device : devices.values()) {
            if (clazz.isInstance(device) && device instanceof IController) {
                return (T) device;
            }
        }
        return null;
    }

    /**
     * Use this to add a device to the network.
     *
     * @param tile The tile to be added
     * @return true if possible, false if it failed.
     */
    public boolean addDevice(INetworkTile tile) {
        boolean controllerExists = getController(tile.getClass()) != null;
        if (!controllerExists) {
            this.devices.putIfAbsent(tile.getPosition(), tile);
            if (tile instanceof INameController) {
                this.setName(((INameController) tile).getNetworkName());
            }
            return true;
        }
        return false;
    }

    /**
     * Use this to remove a device from the network.
     *
     * @param tile The tile to be added
     * @return true if possible, false if it failed.
     */
    public boolean removeDevice(INetworkTile tile) {
        return this.devices.remove(tile.getPosition()) != null;
    }

    /**
     * Use this to get a device on a certain position from the network.
     *
     * @param pos The BlockPos of the required device
     * @return if loaded and existing, the device, null otherwise
     */
    @Nullable
    public INetworkTile getConnectedDevice(BlockPos pos) {
        return world.isBlockLoaded(pos) ? devices.get(pos) : null;
    }

    /**
     * This gives back a Collection of all the connected devices.
     *
     * @return the collection
     */
    public Collection<INetworkTile> getAllDevices() {
        return devices.values();
    }

    // Getters and Setters

    public World getWorld() {
        return world;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    //Utility Functions

    private boolean isSplitted() {
        HashMap<BlockPos, INetworkTile> tempmap = new HashMap<>();
        recursiveSearch((BlockPos) devices.keySet().toArray()[0], null, tempmap);
        return tempmap.keySet().equals(devices.keySet());
    }

    private void recursiveSearch(BlockPos pos, @Nullable EnumFacing from, HashMap<BlockPos, INetworkTile> tempmap) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (!facing.equals(from) && te instanceof INetworkTile & te != null) {
                tempmap.put(pos.offset(facing), (INetworkTile) te);
                recursiveSearch(pos.offset(facing), facing.getOpposite(), tempmap);
            }
        }
    }

    public void splitNetwork() {
        // set all networktile to refresh their network.
        if (isSplitted()) {
            for (INetworkTile tile : devices.values()) {
                tile.setNetwork(null);
            }
        }
    }

    public void mergeNetwork(OMLibNetwork network) {
        // add all the devices from other network to this.
        for (INetworkTile tile : network.getAllDevices()) {
            devices.putIfAbsent(tile.getPosition(), tile);
            tile.setNetwork(this);
        }
        network.destroy();
    }

    public NBTTagCompound getAsNBTTagCompound() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("uuid", this.uuid);
        tag.setString("name", this.name);
        return tag;
    }

    public UUID getUuidFromTagCompound(NBTTagCompound tag) {
        return tag.getUniqueId("uuid");
    }

    private void destroy() {
        OMLibEventHandler.getInstance().removeNetwork(this);
        this.devices = null;
        this.world = null;
    }
}
