package omtteam.omlib.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * A container whose slots may hold more than one stack of an item. The limit is read by the menu slots
 * ({@link omtteam.omlib.menu.LargeStackSlot}) and by the loader adapters of {@link ItemAccess}; such stacks are saved
 * by {@link omtteam.omlib.compat.VersionCompat#saveItems} and synced by {@link omtteam.omlib.menu.LargeStackMenu}.
 * A menu's client-side container implements it too, so the client predicts clicks with the same limits.
 */
public interface LargeStackContainer extends Container {
    /**
     * The most items like {@code stack} that slot {@code slot} may hold; may be above the item's max stack size.
     * {@code stack} may be empty: then the limit for an item with the default max stack size.
     */
    int getSlotLimit(int slot, ItemStack stack);
}
