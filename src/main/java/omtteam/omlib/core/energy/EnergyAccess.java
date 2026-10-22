package omtteam.omlib.core.energy;

/**
 * Energy that a tile entity exposes to other blocks, free of Minecraft and Forge: an {@link EnergyStore}, or a
 * view over several. The Forge adapter ({@code OMEnergyStorage}) turns it into Forge Energy. Negative arguments
 * count as 0.
 */
public interface EnergyAccess {
    /** Energy stored now. */
    long getAmount();

    /** Most energy that can be stored. */
    long getCapacity();

    /** @return the amount accepted (or that would be, if simulated) */
    long insert(long maxAmount, boolean simulate);

    /** @return the amount extracted (or that would be, if simulated) */
    long extract(long maxAmount, boolean simulate);
}
