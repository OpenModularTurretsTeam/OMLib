package omtteam.omlib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Slots of a machine as other blocks see them (hoppers, pipes; the item handler capability): only what the slots
 * accept ({@link IItemHandler#isItemValid}) goes in, and only stacks the extract rule allows come out (OML's lights:
 * nothing; OMT's generator: leftovers such as empty buckets, not fuel).
 */
public final class AutomationItemHandler implements IItemHandler {
    private final IItemHandlerModifiable slots;
    private final Predicate<ItemStack> canExtract;

    /** @param canExtract whether the stack in a slot may be taken out */
    public AutomationItemHandler(IItemHandlerModifiable slots, Predicate<ItemStack> canExtract) {
        this.slots = slots;
        this.canExtract = canExtract;
    }

    @Override
    public int getSlots() {
        return slots.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!slots.isItemValid(slot, stack)) {
            return stack;
        }
        return slots.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = slots.getStackInSlot(slot);
        if (stack.isEmpty() || !canExtract.test(stack)) {
            return ItemStack.EMPTY;
        }
        return slots.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slots.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slots.isItemValid(slot, stack);
    }
}
