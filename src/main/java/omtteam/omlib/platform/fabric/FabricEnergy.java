package omtteam.omlib.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.core.energy.EnergyStore;
import omtteam.omlib.platform.Energy;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Optional;

public final class FabricEnergy implements Energy {
    @Override
    public boolean isBattery(ItemStack stack) {
        if (stack.isEmpty()) return false;
        EnergyStorage storage = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        return storage != null && storage.supportsExtraction();
    }

    @Override
    public long dischargeItem(Container container, int slot, EnergyStore target, long maxAmount) {
        // a slot context, so that the battery's changed data is written back into the container
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(InventoryStorage.of(container, null).getSlot(slot));
        EnergyStorage battery = EnergyStorage.ITEM.find(context.getItemVariant().toStack(), context);
        return battery == null ? 0 : EnergyStorageUtil.move(battery, FabricEnergyStorages.of(target), maxAmount, null);
    }

    @Override
    public Optional<EnergyAccess> itemEnergy(Container container, int slot) {
        if (container.getItem(slot).isEmpty() || find(container, slot) == null) return Optional.empty();
        return Optional.of(new ItemEnergy(container, slot));
    }

    /** The energy storage of the item in the slot, with a slot context so that changes are written back. */
    private static EnergyStorage find(Container container, int slot) {
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(InventoryStorage.of(container, null).getSlot(slot));
        return EnergyStorage.ITEM.find(context.getItemVariant().toStack(), context);
    }

    /** Looks the storage up again on every call: the item in the slot changes with every transfer. */
    private record ItemEnergy(Container container, int slot) implements EnergyAccess {
        @Override
        public long getAmount() {
            EnergyStorage storage = find(container, slot);
            return storage == null ? 0 : storage.getAmount();
        }

        @Override
        public long getCapacity() {
            EnergyStorage storage = find(container, slot);
            return storage == null ? 0 : storage.getCapacity();
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            EnergyStorage storage = find(container, slot);
            if (storage == null || maxAmount <= 0) return 0;
            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.insert(maxAmount, transaction);
                if (!simulate) transaction.commit();
                return inserted;
            }
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            EnergyStorage storage = find(container, slot);
            if (storage == null || maxAmount <= 0) return 0;
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = storage.extract(maxAmount, transaction);
                if (!simulate) transaction.commit();
                return extracted;
            }
        }
    }

    @Override
    public long pushToBlock(Level level, BlockPos pos, Direction side, EnergyStore source, long maxAmount) {
        if (!level.isLoaded(pos)) return 0;
        EnergyStorage target = EnergyStorage.SIDED.find(level, pos, side);
        return target == null ? 0 : EnergyStorageUtil.move(FabricEnergyStorages.of(source), target, maxAmount, null);
    }
}
