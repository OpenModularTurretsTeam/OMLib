package omtteam.omlib.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import omtteam.omlib.inventory.LargeStackContainer;

/**
 * Menu slot of a {@link LargeStackContainer}: it accepts items up to the container's slot limit, but hands out at
 * most one normal stack at a time, so the cursor and the player's inventory never hold more than vanilla allows.
 * Used in a {@link LargeStackMenu}, which handles the clicks a slot cannot (hotbar swap, shift-click on 1.20.1) and
 * syncs the counts.
 */
public class LargeStackSlot extends Slot {
    private boolean hidden;

    public LargeStackSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return container instanceof LargeStackContainer large
                ? large.getSlotLimit(getContainerSlot(), stack) : super.getMaxStackSize(stack);
    }

    /** At most one normal stack (pickup, half pickup, throwing and hotbar swaps all take through here). */
    @Override
    public ItemStack remove(int amount) {
        return super.remove(Math.min(amount, getItem().getMaxStackSize()));
    }

    @Override
    public ItemStack getItem() {
        return hidden ? ItemStack.EMPTY : super.getItem();
    }

    /**
     * Client rendering only: while hidden, the slot looks empty, so vanilla's slot rendering skips it and the
     * library's {@code LargeStackScreen} draws it with its own count. Never left on outside a frame.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
