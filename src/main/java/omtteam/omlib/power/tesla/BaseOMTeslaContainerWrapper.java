package omtteam.omlib.power.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.tileentity.TileEntityElectric;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */

public class BaseOMTeslaContainerWrapper implements ITeslaConsumer {

    private final TileEntityElectric tile;
    private final EnumFacing facing;

    @SuppressWarnings("SameParameterValue")
    public BaseOMTeslaContainerWrapper(TileEntityElectric tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    @Override
    public long givePower(long power, boolean simulated) {
        OMEnergyStorage storage = (OMEnergyStorage) tile.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.receiveEnergy((int) power, simulated);
        }
        return 0;
    }
}