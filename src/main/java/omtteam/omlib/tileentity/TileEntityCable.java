package omtteam.omlib.tileentity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import omtteam.omlib.api.IDebugTile;
import omtteam.omlib.api.IHasOwner;
import omtteam.omlib.api.network.INetworkCable;
import omtteam.omlib.api.network.INetworkTile;
import omtteam.omlib.api.network.OMLibNetwork;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityCable extends TileEntityBase implements INetworkCable, IDebugTile, IHasOwner {
    private Map<EnumFacing, Boolean> facingMap = new HashMap<>();
    private OMLibNetwork network;

    public TileEntityCable() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            facingMap.put(facing, false);
        }
    }

    @Override
    public boolean shouldConnect(EnumFacing side) {
        return facingMap.get(side);
    }

    @Override
    public OMLibNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(OMLibNetwork network) {
        this.network = network;
    }

    @Nonnull
    @Override
    public String getDeviceName() {
        return "networkCable";
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.getPos();
    }

    @Override
    public List<String> getDebugInfo() {
        List<String> debug = new ArrayList<>();
        debug.add("Network = " + this.getNetwork().getUuid().toString());
        return debug;
    }

    private void refreshFacingMap() {
        facingMap = new HashMap<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (this.getWorld().getTileEntity(this.getPos().offset(facing)) instanceof INetworkTile) {
                facingMap.put(facing, true);
            } else {
                facingMap.put(facing, false);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshFacingMap();
        if (this.network == null) {
            this.scan(this.getWorld(), this.getPos(), "");
        }
    }

    @Override
    @Nonnull
    public String getOwner() {
        return network.getOwner().toString();
    }

    @Override
    @Nonnull
    public String getOwnerName() {
        return network.getOwnerName();
    }

    @Override
    public String getOwnerTeamName() {
        return ""; //TODO: implement this
    }
}
