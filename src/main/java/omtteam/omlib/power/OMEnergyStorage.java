package omtteam.omlib.power;

import net.minecraftforge.energy.IEnergyStorage;
import omtteam.omlib.core.energy.EnergyStore;

/**
 * Forge Energy adapter around a core {@link EnergyStore}, which holds the energy. Amounts are clamped to
 * {@code int} for Forge; {@code receiveEnergy} / {@code extractEnergy} are limited by maxReceive / maxExtract, the
 * owner's methods ({@link #setEnergyStored}, {@link #modifyEnergyStored}) are not.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class OMEnergyStorage implements IEnergyStorage {
    private final EnergyStore store;

    public OMEnergyStorage(int capacity) {
        this(capacity, capacity, capacity);
    }

    public OMEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public OMEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        // the tile entity saves the amount with its other data, as in 3.x
        this(new EnergyStore(capacity, maxReceive, maxExtract, () -> {
        }));
    }

    /** An adapter around a store owned by someone else (e.g. the core logic of a machine). */
    public OMEnergyStorage(EnergyStore store) {
        this.store = store;
    }

    /** The core store behind this adapter. */
    public EnergyStore store() {
        return store;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) store.insert(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return (int) store.extract(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return clamp(store.getAmount());
    }

    /** Sets the stored energy, clamped to 0 .. capacity. */
    public void setEnergyStored(int stored) {
        store.setAmountUnchecked(Math.min(Math.max(0, stored), store.getCapacity()));
    }

    @Override
    public int getMaxEnergyStored() {
        return clamp(store.getCapacity());
    }

    public int getMaxReceive() {
        return clamp(store.getMaxInsert());
    }

    public void setMaxReceive(int max) {
        store.setMaxInsert(max);
    }

    public int getMaxExtract() {
        return clamp(store.getMaxExtract());
    }

    /** Lowers the stored energy if it exceeds the new capacity. */
    public void setCapacity(int max) {
        store.setCapacity(max);
    }

    /** Adds (positive) or removes (negative) energy, within 0 .. capacity. */
    public void modifyEnergyStored(int change) {
        if (change >= 0) {
            store.generate(change);
        } else {
            store.drain(-(long) change);
        }
    }

    @Override
    public boolean canExtract() {
        return store.getMaxExtract() > 0;
    }

    @Override
    public boolean canReceive() {
        return store.getMaxInsert() > 0;
    }

    private static int clamp(long value) {
        return (int) Math.min(Integer.MAX_VALUE, value);
    }
}
