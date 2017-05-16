package omtteam.omlib.power;

import net.minecraftforge.energy.IEnergyStorage;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
public class OMEnergyStorage implements IEnergyStorage {
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public OMEnergyStorage(int capacity) {
        this(capacity, capacity, capacity);
    }

    public OMEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public OMEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate)
            energy += energyReceived;
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate)
            energy -= energyExtracted;
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public void setMaxReceive(int max) {
        maxReceive = max;
    }

    public void setCapacity(int max) {
        capacity = max;
    }

    public void setEnergyStored(int stored) {
        energy = stored > 0 ? Math.min(stored, capacity) : 0;
    }

    public void modifyEnergyStored(int change) {
        if (change >= 0) {
            energy = Math.min(energy + change, capacity);
        } else {
            energy = Math.max(energy - change, 0);
        }
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }
}