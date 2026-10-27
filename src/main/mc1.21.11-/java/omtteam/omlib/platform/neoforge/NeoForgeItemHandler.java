package omtteam.omlib.platform.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import omtteam.omlib.inventory.ItemAccess;
import omtteam.omlib.inventory.LargeStackContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge view ({@code ResourceHandler<ItemResource>}) of an {@link ItemAccess}: NeoForge's container wrapper
 * (transactions, rollback), limited to the slot range and filtered.
 * A {@link LargeStackContainer} gets slots of its own, because the loader's wrapper stops at the container's max stack
 * size for the item.
 */
public final class NeoForgeItemHandler extends DelegatingResourceHandler<ItemResource> {
    private final ItemAccess access;

    public NeoForgeItemHandler(ItemAccess access) {
        super(delegate(access));
        this.access = access;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return canInsert(resource) && super.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return canInsert(resource) ? super.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return canInsert(resource) ? super.insert(resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return canExtract(resource) ? super.extract(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return canExtract(resource) ? super.extract(resource, amount, transaction) : 0;
    }

    private boolean canInsert(ItemResource resource) {
        return !resource.isEmpty() && access.canInsert().test(resource.toStack());
    }

    private boolean canExtract(ItemResource resource) {
        return !resource.isEmpty() && access.canExtract().test(resource.toStack());
    }

    private static ResourceHandler<ItemResource> delegate(ItemAccess access) {
        if (access.container() instanceof LargeStackContainer large) {
            RootCommitJournal changed = new RootCommitJournal(large::setChanged);
            List<ResourceHandler<ItemResource>> slots = new ArrayList<>(access.slotCount());
            for (int slot = access.firstSlot(); slot < access.firstSlot() + access.slotCount(); slot++) {
                slots.add(new LargeSlot(large, slot, changed));
            }
            return new CombinedResourceHandler<>(slots);
        }
        return RangedResourceHandler.of(VanillaContainerWrapper.of(access.container()), access.firstSlot(),
                access.firstSlot() + access.slotCount());
    }

    /** One slot of a {@link LargeStackContainer}, holding up to its slot limit; marked changed once per root commit. */
    private static final class LargeSlot extends ItemStackResourceHandler {
        private final LargeStackContainer container;
        private final int slot;
        private final RootCommitJournal changed;

        LargeSlot(LargeStackContainer container, int slot, RootCommitJournal changed) {
            this.container = container;
            this.slot = slot;
            this.changed = changed;
        }

        @Override
        protected ItemStack getStack() {
            return container.getItem(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            container.setItem(slot, stack, true);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return container.canPlaceItem(slot, resource.toStack());
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            return container.getSlotLimit(slot, resource.isEmpty() ? ItemStack.EMPTY : resource.toStack());
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            super.updateSnapshots(transaction);
            changed.updateSnapshots(transaction);
        }
    }
}
