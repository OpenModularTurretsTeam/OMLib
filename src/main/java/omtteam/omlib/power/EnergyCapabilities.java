package omtteam.omlib.power;

import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.power.rf.BaseRFReceiverWrapper;
import omtteam.omlib.power.rf.RFCapabilities;
import omtteam.omlib.power.tesla.BaseOMTeslaReceiverWrapper;

import javax.annotation.Nullable;

import static omtteam.omlib.compat.OMLibModCompatibility.CoFHApiLoaded;
import static omtteam.omlib.compat.OMLibModCompatibility.CoFHApiModId;
import static omtteam.omlib.compat.OMLibModCompatibility.TeslaLoaded;
import static omtteam.omlib.compat.OMLibModCompatibility.TeslaModId;

/**
 * The energy side of a tile entity, owned by it (until 4.0.0 the base class {@code TileEntityElectric}): one
 * {@link OMEnergyStorage} exposed as Forge Energy, and as Redstone Flux and Tesla receiver when those mods are loaded,
 * with the 3.x NBT keys {@code maxStorage}, {@code energyStored} and {@code maxIO}.
 * The tile entity forwards {@code hasCapability} / {@code getCapability} to it.
 */
public final class EnergyCapabilities {
    private final OMEnergyStorage storage;
    @Nullable
    private Object rfReceiver;
    @Nullable
    private Object teslaReceiver;

    public EnergyCapabilities(OMEnergyStorage storage) {
        this.storage = storage;
    }

    public OMEnergyStorage getStorage() {
        return storage;
    }

    public boolean hasCapability(Capability<?> capability) {
        return capability == CapabilityEnergy.ENERGY
                || (TeslaLoaded && isTeslaCapability(capability))
                || (CoFHApiLoaded && isRFCapability(capability));
    }

    /** The capability instance, or null if it is not an energy capability of this storage. */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getCapability(Capability<T> capability) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) storage;
        }
        if (TeslaLoaded && isTeslaCapability(capability)) {
            return (T) teslaReceiver();
        }
        if (CoFHApiLoaded && isRFCapability(capability)) {
            return (T) rfReceiver();
        }
        return null;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("maxStorage", storage.getMaxEnergyStored());
        tag.setInteger("energyStored", storage.getEnergyStored());
        tag.setInteger("maxIO", storage.getMaxReceive());
    }

    public void readFromNBT(NBTTagCompound tag) {
        storage.setCapacity(tag.getInteger("maxStorage"));
        storage.setEnergyStored(tag.getInteger("energyStored"));
        storage.setMaxReceive(tag.getInteger("maxIO"));
    }

    @Optional.Method(modid = CoFHApiModId)
    private boolean isRFCapability(Capability<?> capability) {
        return capability == RFCapabilities.CAPABILITY_CONSUMER;
    }

    @Optional.Method(modid = CoFHApiModId)
    private Object rfReceiver() {
        if (rfReceiver == null) {
            rfReceiver = new BaseRFReceiverWrapper(storage);
        }
        return rfReceiver;
    }

    @Optional.Method(modid = TeslaModId)
    private boolean isTeslaCapability(Capability<?> capability) {
        return capability == TeslaCapabilities.CAPABILITY_CONSUMER;
    }

    @Optional.Method(modid = TeslaModId)
    private Object teslaReceiver() {
        if (teslaReceiver == null) {
            teslaReceiver = new BaseOMTeslaReceiverWrapper(storage);
        }
        return teslaReceiver;
    }
}
