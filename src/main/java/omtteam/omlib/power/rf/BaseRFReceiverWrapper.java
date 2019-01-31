package omtteam.omlib.power.rf;

import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.tileentity.TileEntityElectric;

import static omtteam.omlib.compatibility.OMLibModCompatibility.CoFHApiModId;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */

public class BaseRFReceiverWrapper implements IEnergyReceiver {

    private final TileEntityElectric tile;
    private final EnumFacing facing;

    @SuppressWarnings("SameParameterValue")
    public BaseRFReceiverWrapper(TileEntityElectric tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        OMEnergyStorage storage = (OMEnergyStorage) tile.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.receiveEnergy(maxReceive, simulate);
        }
        return maxReceive;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        OMEnergyStorage storage = (OMEnergyStorage) tile.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.getEnergyStored();
        }
        return 0;
    }

    @Optional.Method(modid = CoFHApiModId)
    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        OMEnergyStorage storage = (OMEnergyStorage) tile.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.getMaxEnergyStored();
        }
        return 0;
    }

    @Optional.Method(modid = CoFHApiModId)
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }
}