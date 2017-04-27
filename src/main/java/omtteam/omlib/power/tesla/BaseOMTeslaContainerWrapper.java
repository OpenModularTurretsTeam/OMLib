package omtteam.omlib.power.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import omtteam.omlib.tileentity.TileEntityElectric;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */
@SuppressWarnings("WeakerAccess")
public class BaseOMTeslaContainerWrapper implements ITeslaConsumer {

    private final TileEntityElectric tile;
    private final EnumFacing facing;

    public BaseOMTeslaContainerWrapper(TileEntityElectric tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    @Override
    public long givePower(long power, boolean simulated) {
        return tile.receiveEnergy(facing, (int) power, simulated);
    }

}