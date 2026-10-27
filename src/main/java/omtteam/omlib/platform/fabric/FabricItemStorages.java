package omtteam.omlib.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import omtteam.omlib.inventory.ItemAccess;
import omtteam.omlib.inventory.LargeStackContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fabric Transfer API view of an {@link ItemAccess}: Fabric's {@link InventoryStorage} (transactions, rollback),
 * limited to the slot range and filtered. It stays slotted, so mods that read inventories slot by slot (Jade) still see
 * the contents. A {@link LargeStackContainer} gets slots of its own, because Fabric's wrapper stops at the item's max
 * stack size.
 */
public final class FabricItemStorages {
    private FabricItemStorages() {
    }

    public static SlottedStorage<ItemVariant> of(ItemAccess access) {
        if (access.container() instanceof LargeStackContainer large) {
            List<SingleSlotStorage<ItemVariant>> slots = new ArrayList<>(access.slotCount());
            for (int slot = access.firstSlot(); slot < access.firstSlot() + access.slotCount(); slot++) {
                slots.add(new LargeSlot(large, slot, access.canInsert(), access.canExtract()));
            }
            return new CombinedSlottedStorage<>(slots);
        }
        List<SingleSlotStorage<ItemVariant>> slots = InventoryStorage.of(access.container(), null).getSlots()
                .subList(access.firstSlot(), access.firstSlot() + access.slotCount());
        return new CombinedSlottedStorage<>(slots.stream()
                .<SingleSlotStorage<ItemVariant>>map(slot -> new FilteredSlot(slot, access.canInsert(), access.canExtract()))
                .toList());
    }

    private record FilteredSlot(SingleSlotStorage<ItemVariant> slot, Predicate<ItemStack> canInsert,
                                Predicate<ItemStack> canExtract) implements SingleSlotStorage<ItemVariant> {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return canInsert.test(resource.toStack()) ? slot.insert(resource, maxAmount, transaction) : 0;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return canExtract.test(resource.toStack()) ? slot.extract(resource, maxAmount, transaction) : 0;
        }

        @Override
        public boolean isResourceBlank() {
            return slot.isResourceBlank();
        }

        @Override
        public ItemVariant getResource() {
            return slot.getResource();
        }

        @Override
        public long getAmount() {
            return slot.getAmount();
        }

        @Override
        public long getCapacity() {
            return slot.getCapacity();
        }
    }

    /**
     * One slot of a {@link LargeStackContainer}, holding up to its slot limit. The container's {@code canPlaceItem}
     * applies as with Fabric's wrapper; it is marked changed once per outermost commit.
     */
    private static final class LargeSlot extends SingleStackStorage {
        private final LargeStackContainer container;
        private final int slot;
        private final Predicate<ItemStack> canInsert;
        private final Predicate<ItemStack> canExtract;

        LargeSlot(LargeStackContainer container, int slot, Predicate<ItemStack> canInsert, Predicate<ItemStack> canExtract) {
            this.container = container;
            this.slot = slot;
            this.canInsert = canInsert;
            this.canExtract = canExtract;
        }

        @Override
        protected ItemStack getStack() {
            return container.getItem(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            container.setItem(slot, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant variant) {
            ItemStack stack = variant.toStack();
            return canInsert.test(stack) && container.canPlaceItem(slot, stack);
        }

        @Override
        protected boolean canExtract(ItemVariant variant) {
            return canExtract.test(variant.toStack());
        }

        @Override
        protected int getCapacity(ItemVariant variant) {
            return container.getSlotLimit(slot, variant.toStack());
        }

        @Override
        protected void onFinalCommit() {
            container.setChanged();
        }
    }
}
