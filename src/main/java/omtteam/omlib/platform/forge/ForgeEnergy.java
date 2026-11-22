package omtteam.omlib.platform.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import omtteam.omlib.core.energy.EnergyAccess;
import omtteam.omlib.core.energy.EnergyStore;
import omtteam.omlib.platform.Energy;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ForgeEnergy implements Energy {
    @Override
    public boolean isBattery(ItemStack stack) {
        IEnergyStorage storage = storage(stack);
        return storage != null && storage.canExtract();
    }

    @Override
    public long dischargeItem(Container container, int slot, EnergyStore target, long maxAmount) {
        IEnergyStorage battery = storage(container.getItem(slot));
        if (battery == null) return 0;
        // the item capability changes the stack in the slot directly (its NBT)
        long accepted = target.insert(battery.extractEnergy(ForgeEnergyHandler.clamp(maxAmount), true), true);
        int moved = battery.extractEnergy(ForgeEnergyHandler.clamp(accepted), false);
        target.insert(moved, false);
        if (moved > 0) container.setChanged();
        return moved;
    }

    @Override
    public Optional<EnergyAccess> itemEnergy(Container container, int slot) {
        if (storage(container.getItem(slot)) == null) return Optional.empty();
        return Optional.of(new ItemEnergy(container, slot));
    }

    /**
     * Looks the storage up again on every call: the item in the slot may change. The item capability changes the stack
     * in the slot directly (its NBT).
     */
    private record ItemEnergy(Container container, int slot) implements EnergyAccess {
        @Override
        public long getAmount() {
            IEnergyStorage storage = storage(container.getItem(slot));
            return storage == null ? 0 : storage.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            IEnergyStorage storage = storage(container.getItem(slot));
            return storage == null ? 0 : storage.getMaxEnergyStored();
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            IEnergyStorage storage = storage(container.getItem(slot));
            if (storage == null || maxAmount <= 0) return 0;
            int inserted = storage.receiveEnergy(ForgeEnergyHandler.clamp(maxAmount), simulate);
            if (!simulate && inserted > 0) container.setChanged();
            return inserted;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            IEnergyStorage storage = storage(container.getItem(slot));
            if (storage == null || maxAmount <= 0) return 0;
            int extracted = storage.extractEnergy(ForgeEnergyHandler.clamp(maxAmount), simulate);
            if (!simulate && extracted > 0) container.setChanged();
            return extracted;
        }
    }

    @Override
    public long pushToBlock(Level level, BlockPos pos, Direction side, EnergyStore source, long maxAmount) {
        if (!level.isLoaded(pos)) return 0;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return 0;
        IEnergyStorage target = blockEntity.getCapability(ForgeCapabilities.ENERGY, side).resolve().orElse(null);
        if (target == null) return 0;
        int accepted = target.receiveEnergy(ForgeEnergyHandler.clamp(source.extract(maxAmount, true)), false);
        source.extract(accepted, false);
        return accepted;
    }

    @Nullable
    private static IEnergyStorage storage(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
    }
}
