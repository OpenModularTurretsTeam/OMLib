package omtteam.omlib.power.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import omtteam.omlib.tileentity.TileEntityMachine;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */
@SuppressWarnings("WeakerAccess")
public class BaseOMTeslaContainerWrapper implements ITeslaConsumer {

    private final TileEntityMachine tile;
    private final EnumFacing facing;

    public BaseOMTeslaContainerWrapper(TileEntityMachine tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    @Override
    public long givePower(long power, boolean simulated) {
        return tile.receiveEnergy(facing, (int) power, simulated);
    }

}