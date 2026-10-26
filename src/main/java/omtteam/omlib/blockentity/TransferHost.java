package omtteam.omlib.blockentity;

import net.minecraft.core.Direction;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.inventory.ItemAccess;
import org.jetbrains.annotations.Nullable;

/**
 * A block entity whose items and energy other blocks may use (hoppers, cables, machines). The loader adapters wrap
 * what it returns; each loader registers its block entity types from {@code ModBlockEntities#transferTypes}.
 */
public interface TransferHost {
    /** The slots that other blocks may use on {@code side} ({@code null}: no side). */
    ItemAccess getItemAccess(@Nullable Direction side);

    /** The energy that other blocks may use on {@code side} ({@code null}: no side), or {@code null} for none. */
    @Nullable
    EnergyAccess getEnergyAccess(@Nullable Direction side);
}
