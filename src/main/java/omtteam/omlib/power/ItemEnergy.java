package omtteam.omlib.power;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import omtteam.omlib.core.energy.EnergyAccess;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The energy of an item in an inventory slot, as on 1.20.1, 1.21.1 and 1.21.11 ({@code Energy.itemEnergy}): Forge
 * Energy ({@link CapabilityEnergy#ENERGY}, 1 E = 1 FE). Added for the energy drain turret of Open Modular Turrets.
 */
public final class ItemEnergy {
    private ItemEnergy() {
    }

    /**
     * A live view of the energy of the item in {@code slot} of {@code inventory}: every call reads or changes the item
     * that is in the slot at that moment (the item capability changes the stack itself), and a change marks the
     * inventory dirty. Its {@code insert} and {@code extract} follow the item's own limits; a simulated call changes
     * nothing.
     *
     * @return empty if the slot is empty or its item stores no energy
     */
    public static Optional<EnergyAccess> of(IInventory inventory, int slot) {
        if (storage(inventory, slot) == null) return Optional.empty();
        return Optional.of(new SlotEnergy(inventory, slot));
    }

    @Nullable
    private static IEnergyStorage storage(IInventory inventory, int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (stack.isEmpty() || !stack.hasCapability(CapabilityEnergy.ENERGY, null)) return null;
        return stack.getCapability(CapabilityEnergy.ENERGY, null);
    }

    /** Looks the storage up again on every call: the item in the slot may change. */
    private static final class SlotEnergy implements EnergyAccess {
        private final IInventory inventory;
        private final int slot;

        private SlotEnergy(IInventory inventory, int slot) {
            this.inventory = inventory;
            this.slot = slot;
        }

        @Override
        public long getAmount() {
            IEnergyStorage storage = storage(inventory, slot);
            return storage == null ? 0 : storage.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            IEnergyStorage storage = storage(inventory, slot);
            return storage == null ? 0 : storage.getMaxEnergyStored();
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            IEnergyStorage storage = storage(inventory, slot);
            if (storage == null || maxAmount <= 0) return 0;
            int inserted = storage.receiveEnergy(clamp(maxAmount), simulate);
            if (!simulate && inserted > 0) inventory.markDirty();
            return inserted;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            IEnergyStorage storage = storage(inventory, slot);
            if (storage == null || maxAmount <= 0) return 0;
            int extracted = storage.extractEnergy(clamp(maxAmount), simulate);
            if (!simulate && extracted > 0) inventory.markDirty();
            return extracted;
        }

        private static int clamp(long amount) {
            return (int) Math.min(amount, Integer.MAX_VALUE);
        }
    }
}
