package omtteam.omlib.power;

import net.minecraftforge.energy.IEnergyStorage;
import omtteam.omlib.core.energy.EnergyAccess;

/**
 * An {@link EnergyAccess} as Forge Energy (1 E = 1 FE) for cables and other mods (moved from OpenModularLighting).
 * Forge Energy counts in {@code int}, so amounts are clamped to {@link Integer#MAX_VALUE}; a storage that is not
 * {@code extractable} only receives.
 */
public final class EnergyAccessStorage implements IEnergyStorage {
    private final EnergyAccess access;
    private final boolean extractable;

    public EnergyAccessStorage(EnergyAccess access, boolean extractable) {
        this.access = access;
        this.extractable = extractable;
    }

    /** The wrapped access, so a caller can tell whether a cached adapter still fits. */
    public EnergyAccess access() {
        return access;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) access.insert(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return extractable ? (int) access.extract(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored() {
        return clamp(access.getAmount());
    }

    @Override
    public int getMaxEnergyStored() {
        return clamp(access.getCapacity());
    }

    @Override
    public boolean canExtract() {
        return extractable;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private static int clamp(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }
}
