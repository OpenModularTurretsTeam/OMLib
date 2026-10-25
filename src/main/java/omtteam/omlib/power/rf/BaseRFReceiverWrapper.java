package omtteam.omlib.power.rf;

import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.energy.IEnergyStorage;

import static omtteam.omlib.compat.OMLibModCompatibility.CoFHApiModId;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */

public class BaseRFReceiverWrapper implements IEnergyReceiver {

    private final IEnergyStorage storage;

    public BaseRFReceiverWrapper(IEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return storage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return storage.getEnergyStored();
    }

    @Optional.Method(modid = CoFHApiModId)
    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return storage.getMaxEnergyStored();
    }

    @Optional.Method(modid = CoFHApiModId)
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }
}
