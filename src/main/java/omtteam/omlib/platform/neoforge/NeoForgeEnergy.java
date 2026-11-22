package omtteam.omlib.platform.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.core.energy.EnergyStore;
import omtteam.omlib.platform.Energy;

import java.util.Optional;

public final class NeoForgeEnergy implements Energy {
    /** Any item with an energy capacity, as NeoForge's own {@code IEnergyStorage} adapter answers {@code canExtract}. */
    @Override
    public boolean isBattery(ItemStack stack) {
        if (stack.isEmpty()) return false;
        EnergyHandler battery = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
        return battery != null && battery.getCapacityAsLong() > 0;
    }

    @Override
    public long dischargeItem(Container container, int slot, EnergyStore target, long maxAmount) {
        if (container.getItem(slot).isEmpty()) return 0;
        // the item's access writes the changed battery back into the slot when the transaction commits
        EnergyHandler battery = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(container), slot)
                .getCapability(Capabilities.Energy.ITEM);
        if (battery == null) return 0;
        int room = NeoForgeEnergyHandler.clamp(target.insert(maxAmount, true));
        if (room == 0) return 0;
        int moved;
        try (Transaction transaction = Transaction.openRoot()) {
            moved = battery.extract(room, transaction);
            transaction.commit();
        }
        target.insert(moved, false);
        if (moved > 0) container.setChanged();
        return moved;
    }

    @Override
    public Optional<EnergyAccess> itemEnergy(Container container, int slot) {
        if (container.getItem(slot).isEmpty() || find(container, slot) == null) return Optional.empty();
        return Optional.of(new ItemEnergy(container, slot));
    }

    /** The energy handler of the item in the slot; its item access writes changes back when a transaction commits. */
    private static EnergyHandler find(Container container, int slot) {
        if (container.getItem(slot).isEmpty()) return null;
        return ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(container), slot).getCapability(Capabilities.Energy.ITEM);
    }

    /** Looks the handler up again on every call: the item in the slot changes with every transfer. */
    private record ItemEnergy(Container container, int slot) implements EnergyAccess {
        @Override
        public long getAmount() {
            EnergyHandler handler = find(container, slot);
            return handler == null ? 0 : handler.getAmountAsLong();
        }

        @Override
        public long getCapacity() {
            EnergyHandler handler = find(container, slot);
            return handler == null ? 0 : handler.getCapacityAsLong();
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            EnergyHandler handler = find(container, slot);
            if (handler == null || maxAmount <= 0) return 0;
            int inserted;
            try (Transaction transaction = Transaction.openRoot()) {
                inserted = handler.insert(NeoForgeEnergyHandler.clamp(maxAmount), transaction);
                if (!simulate) transaction.commit();
            }
            if (!simulate && inserted > 0) container.setChanged();
            return inserted;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            EnergyHandler handler = find(container, slot);
            if (handler == null || maxAmount <= 0) return 0;
            int extracted;
            try (Transaction transaction = Transaction.openRoot()) {
                extracted = handler.extract(NeoForgeEnergyHandler.clamp(maxAmount), transaction);
                if (!simulate) transaction.commit();
            }
            if (!simulate && extracted > 0) container.setChanged();
            return extracted;
        }
    }

    @Override
    public long pushToBlock(Level level, BlockPos pos, Direction side, EnergyStore source, long maxAmount) {
        if (!level.isLoaded(pos)) return 0;
        EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        if (target == null) return 0;
        int accepted;
        try (Transaction transaction = Transaction.openRoot()) {
            accepted = target.insert(NeoForgeEnergyHandler.clamp(source.extract(maxAmount, true)), transaction);
            transaction.commit();
        }
        source.extract(accepted, false);
        return accepted;
    }
}
