package omtteam.omlib.power.forge;

import net.minecraft.util.EnumFacing;
import omtteam.omlib.tileentity.TileEntityElectric;

public class InternalRecieverTileWrapper {

    private final TileEntityElectric tile;
    private final EnumFacing facing;

    public InternalRecieverTileWrapper(TileEntityElectric tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        return tile.receiveEnergy(facing, maxReceive, simulate);
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    public boolean canReceive() {
        return true;
    }

}
