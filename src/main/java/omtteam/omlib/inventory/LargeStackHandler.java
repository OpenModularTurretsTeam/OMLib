package omtteam.omlib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * An item handler whose slots may hold more than one stack of an item (1.20.1+ {@code inventory.LargeStackContainer};
 * on 1.12 the inventories are item handlers). The limit is read by the container slots
 * ({@link omtteam.omlib.container.LargeStackSlot}); {@link LargeStackItemHandler} applies it to hoppers and pipes and
 * saves such stacks, {@link omtteam.omlib.container.LargeStackContainer} syncs them.
 * The client side of a container uses the same handler class, so it predicts clicks with the same limits.
 */
public interface LargeStackHandler extends IItemHandlerModifiable {
    /**
     * The most items like {@code stack} that slot {@code slot} may hold; may be above the item's max stack size.
     * {@code stack} may be empty: then the limit for an item with the default max stack size.
     */
    int getSlotLimit(int slot, ItemStack stack);
}
