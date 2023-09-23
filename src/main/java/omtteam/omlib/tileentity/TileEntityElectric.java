package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.compatibility.OMLibModCompatibility;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.power.rf.BaseRFReceiverWrapper;
import omtteam.omlib.power.rf.RFCapabilities;
import omtteam.omlib.power.tesla.BaseOMTeslaReceiverWrapper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.compatibility.OMLibModCompatibility.*;

/**
 * Created by Keridos on 27/04/17.
 * This Class
 */

@SuppressWarnings({"WeakerAccess", "CanBeFinal"})
@Optional.InterfaceList({
        @Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = CoFHApiModId)})
@MethodsReturnNonnullByDefault
public abstract class TileEntityElectric extends TileEntityOwnedBlock {
    protected OMEnergyStorage storage;
    protected Object teslaContainer;
    protected Object euContainer;
    protected Object rfContainer;
    protected boolean wasAddedToEnergyNet = false;

    @Override
    public NBTTagCompound saveToNBT(NBTTagCompound nbtTagCompound) {
        super.saveToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("maxStorage", this.storage.getMaxEnergyStored());
        nbtTagCompound.setInteger("energyStored", this.storage.getEnergyStored());
        nbtTagCompound.setInteger("maxIO", this.storage.getMaxReceive());
        return nbtTagCompound;
    }

    @Override
    public void loadFromNBT(NBTTagCompound nbtTagCompound) {
        super.loadFromNBT(nbtTagCompound);
        this.storage.setCapacity(nbtTagCompound.getInteger("maxStorage"));
        this.storage.setEnergyStored(nbtTagCompound.getInteger("energyStored"));
        this.storage.setMaxReceive(nbtTagCompound.getInteger("maxIO"));
    }

    //-------------------------- Energy API Functions --------------------------------------------------

    public int getEnergyStored(@Nullable EnumFacing facing) {
        OMEnergyStorage storage = (OMEnergyStorage) this.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.getEnergyStored();
        }
        return 0;
    }

    public int getMaxEnergyStored(@Nullable EnumFacing facing) {
        OMEnergyStorage storage = (OMEnergyStorage) this.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.getMaxEnergyStored();
        }
        return 0;
    }

    public void setCapacity(int maxStorage, @Nullable EnumFacing facing) {
        OMEnergyStorage storage = (OMEnergyStorage) this.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            storage.setCapacity(maxStorage);
        }
    }

    public void setEnergyStored(int energy, @Nullable EnumFacing facing) {
        OMEnergyStorage storage = (OMEnergyStorage) this.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            storage.setEnergyStored(energy);
        }
    }

    public int extractEnergy(int energy, @Nullable EnumFacing facing) {
        OMEnergyStorage storage = (OMEnergyStorage) this.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            return storage.extractEnergy(energy, false);
        }
        return 0;
    }

    //--------------------------------------------------------------------------------------------------

    @SuppressWarnings({"unchecked"})
    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (OMLibModCompatibility.TeslaLoaded) {
            if (hasTeslaCapability(capability, facing) && getTeslaCapability(capability, facing) != null) {
                return getTeslaCapability(capability, facing);
            }
        }
        if (CoFHApiLoaded) {
            if (hasRFCapability(capability, facing) && getRFCapability(capability, facing) != null) {
                return getRFCapability(capability, facing);
            }
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) storage;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        // This method replaces the instanceof checks that would be used in an interface based
        // system. It can be used by other things to see if the TileEntity uses a capability or
        // not.
        if (OMLibModCompatibility.TeslaLoaded) {
            if (hasTeslaCapability(capability, facing)) {
                return true;
            }
        }
        if (CoFHApiLoaded) {
            if (hasRFCapability(capability, facing)) {
                return true;
            }
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Optional.Method(modid = CoFHApiModId)
    private boolean hasRFCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == RFCapabilities.CAPABILITY_CONSUMER);
    }

    @Optional.Method(modid = CoFHApiModId)
    @SuppressWarnings({"unchecked"})
    @Nullable
    private <T> T getRFCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == TeslaCapabilities.CAPABILITY_CONSUMER) {
            return (T) getRFContainer();
        }
        return null;
    }

    @Optional.Method(modid = CoFHApiModId)
    private BaseRFReceiverWrapper getRFContainer() {
        if (rfContainer instanceof BaseRFReceiverWrapper) {
            return (BaseRFReceiverWrapper) rfContainer;
        } else {
            rfContainer = new BaseRFReceiverWrapper(this, EnumFacing.DOWN);
            return (BaseRFReceiverWrapper) rfContainer;
        }
    }

    @Optional.Method(modid = TeslaModId)
    private BaseOMTeslaReceiverWrapper getTeslaContainer() {
        if (teslaContainer instanceof BaseOMTeslaReceiverWrapper) {
            return (BaseOMTeslaReceiverWrapper) teslaContainer;
        } else {
            teslaContainer = new BaseOMTeslaReceiverWrapper(this, EnumFacing.DOWN);
            return (BaseOMTeslaReceiverWrapper) teslaContainer;
        }
    }

    @Optional.Method(modid = TeslaModId)
    private boolean hasTeslaCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == TeslaCapabilities.CAPABILITY_CONSUMER);
    }

    @Optional.Method(modid = TeslaModId)
    @SuppressWarnings({"unchecked"})
    @Nullable
    private <T> T getTeslaCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == TeslaCapabilities.CAPABILITY_CONSUMER) {
            return (T) getTeslaContainer();
        }
        return null;
    }
}
