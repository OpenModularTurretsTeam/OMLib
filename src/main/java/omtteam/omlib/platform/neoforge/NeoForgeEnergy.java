package omtteam.omlib.platform.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.core.energy.EnergyStore;
import omtteam.omlib.platform.Energy;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class NeoForgeEnergy implements Energy {
    @Override
    public boolean isBattery(ItemStack stack) {
        if (stack.isEmpty()) return false;
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage != null && storage.canExtract();
    }

    @Override
    public long dischargeItem(Container container, int slot, EnergyStore target, long maxAmount) {
        ItemStack stack = container.getItem(slot);
        IEnergyStorage battery = stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (battery == null) return 0;
        // the item capability changes the stack in the slot directly (data components)
        long accepted = target.insert(battery.extractEnergy(NeoForgeEnergyHandler.clamp(maxAmount), true), true);
        int moved = battery.extractEnergy(NeoForgeEnergyHandler.clamp(accepted), false);
        target.insert(moved, false);
        if (moved > 0) container.setChanged();
        return moved;
    }

    @Override
    public Optional<EnergyAccess> itemEnergy(Container container, int slot) {
        if (find(container, slot) == null) return Optional.empty();
        return Optional.of(new ItemEnergy(container, slot));
    }

    /** The energy storage of the item in the slot; the item capability changes the stack in the slot directly. */
    @Nullable
    private static IEnergyStorage find(Container container, int slot) {
        ItemStack stack = container.getItem(slot);
        return stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    /** Looks the storage up again on every call: the item in the slot may change. */
    private record ItemEnergy(Container container, int slot) implements EnergyAccess {
        @Override
        public long getAmount() {
            IEnergyStorage storage = find(container, slot);
            return storage == null ? 0 : storage.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            IEnergyStorage storage = find(container, slot);
            return storage == null ? 0 : storage.getMaxEnergyStored();
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            IEnergyStorage storage = find(container, slot);
            if (storage == null || maxAmount <= 0) return 0;
            int inserted = storage.receiveEnergy(NeoForgeEnergyHandler.clamp(maxAmount), simulate);
            if (!simulate && inserted > 0) container.setChanged();
            return inserted;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            IEnergyStorage storage = find(container, slot);
            if (storage == null || maxAmount <= 0) return 0;
            int extracted = storage.extractEnergy(NeoForgeEnergyHandler.clamp(maxAmount), simulate);
            if (!simulate && extracted > 0) container.setChanged();
            return extracted;
        }
    }

    @Override
    public long pushToBlock(Level level, BlockPos pos, Direction side, EnergyStore source, long maxAmount) {
        if (!level.isLoaded(pos)) return 0;
        IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
        if (target == null) return 0;
        int accepted = target.receiveEnergy(NeoForgeEnergyHandler.clamp(source.extract(maxAmount, true)), false);
        source.extract(accepted, false);
        return accepted;
    }
}
