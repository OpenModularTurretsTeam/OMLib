package omtteam.omlib.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.core.energy.EnergyStore;

import java.util.Optional;

/**
 * Energy of items and of other mods' blocks, through the loader's energy API (Team Reborn Energy on Fabric, FE on
 * Forge; 1 E = 1 FE).
 */
public interface Energy {
    /**
     * Whether energy can be extracted from this item (a battery slot accepts it).
     */
    boolean isBattery(ItemStack stack);

    /**
     * Moves up to {@code maxAmount} from the battery in {@code slot} of {@code container} into {@code target} (via
     * {@link EnergyStore#insert}, so its {@code maxInsert} applies). The item in the slot is updated.
     *
     * @return the amount moved
     */
    long dischargeItem(Container container, int slot, EnergyStore target, long maxAmount);

    /**
     * A live view of the energy of the item in {@code slot} of {@code container}: every call reads or changes the item
     * that is in the slot at that moment, and a change is written back into the slot. Its {@code insert} and
     * {@code extract} follow the item's own limits; a simulated call changes nothing.
     *
     * @return empty if the slot is empty or its item stores no energy
     */
    Optional<EnergyAccess> itemEnergy(Container container, int slot);

    /**
     * Moves up to {@code maxAmount} from {@code source} (via {@link EnergyStore#extract}, so its {@code maxExtract}
     * applies) into the block at {@code pos}, entering through {@code side} (the face of that block, as in a sided
     * lookup).
     *
     * @return the amount moved; 0 if there is no energy block there or its chunk is not loaded
     */
    long pushToBlock(Level level, BlockPos pos, Direction side, EnergyStore source, long maxAmount);
}
