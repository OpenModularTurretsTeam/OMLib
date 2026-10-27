package omtteam.omlib.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import omtteam.omlib.inventory.LargeStackHandler;

import javax.annotation.Nonnull;

/**
 * Container slot of a {@link LargeStackHandler} (1.20.1+ {@code menu.LargeStackSlot}): it accepts items up to the
 * handler's slot limit, but hands out at most one normal stack at a time, so the cursor and the player's inventory
 * never hold more than vanilla allows. Used in a {@link LargeStackContainer}, which handles the clicks a slot cannot
 * (hotbar swap, swapping with the cursor, shift-click) and syncs the counts.
 */
public class LargeStackSlot extends SlotItemHandler {
    private boolean hidden;

    public LargeStackSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        IItemHandler handler = getItemHandler();
        return handler instanceof LargeStackHandler
                ? ((LargeStackHandler) handler).getSlotLimit(getSlotIndex(), stack) : super.getItemStackLimit(stack);
    }

    @Override
    public int getSlotStackLimit() {
        IItemHandler handler = getItemHandler();
        return handler instanceof LargeStackHandler
                ? ((LargeStackHandler) handler).getSlotLimit(getSlotIndex(), super.getStack()) : super.getSlotStackLimit();
    }

    /** At most one normal stack (pickup, half pickup, throwing and hotbar swaps all take through here). */
    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        return super.decrStackSize(Math.min(amount, super.getStack().getMaxStackSize()));
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return hidden ? ItemStack.EMPTY : super.getStack();
    }

    /**
     * Client rendering only: while hidden, the slot looks empty, so vanilla's slot rendering skips it and the
     * library's {@code LargeStackGui} draws it with its own count. Never left on outside a frame.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
