package omtteam.omlib.platform.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import omtteam.omlib.inventory.ItemAccess;
import omtteam.omlib.inventory.LargeStackContainer;

/**
 * NeoForge view ({@code IItemHandler}) of an {@link ItemAccess}: NeoForge's container wrapper, limited to the slot range
 * and filtered.
 * A {@link LargeStackContainer} gets a wrapper of its own, because the loader's stops at the item's max stack size.
 */
public final class NeoForgeItemHandler extends RangedWrapper {
    private final ItemAccess access;

    public NeoForgeItemHandler(ItemAccess access) {
        super(wrap(access), access.firstSlot(), access.firstSlot() + access.slotCount());
        this.access = access;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return access.canInsert().test(stack) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack inSlot = getStackInSlot(slot);
        return !inSlot.isEmpty() && access.canExtract().test(inSlot) ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return access.canInsert().test(stack) && super.isItemValid(slot, stack);
    }

    private static InvWrapper wrap(ItemAccess access) {
        return access.container() instanceof LargeStackContainer large ? new LargeStackWrapper(large) : new InvWrapper(access.container());
    }

    /**
     * The loader's container wrapper with the slot limits of a {@link LargeStackContainer}: inserts fill a slot up to
     * its limit, extracts hand out at most one normal stack.
     */
    private static final class LargeStackWrapper extends InvWrapper {
        private final LargeStackContainer container;

        LargeStackWrapper(LargeStackContainer container) {
            super(container);
            this.container = container;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack inSlot = container.getItem(slot);
            if (!inSlot.isEmpty() && !ItemStack.isSameItemSameComponents(stack, inSlot)) return stack;
            if (!container.canPlaceItem(slot, stack)) return stack;
            int room = container.getSlotLimit(slot, stack) - inSlot.getCount();
            if (room <= 0) return stack;
            int moved = Math.min(room, stack.getCount());
            if (!simulate) {
                container.setItem(slot, stack.copyWithCount(inSlot.getCount() + moved));
                container.setChanged();
            }
            return moved == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - moved);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return super.extractItem(slot, Math.min(amount, container.getItem(slot).getMaxStackSize()), simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return container.getSlotLimit(slot, container.getItem(slot));
        }
    }
}
