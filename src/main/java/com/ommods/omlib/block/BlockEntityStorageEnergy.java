package com.ommods.omlib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public abstract class BlockEntityStorageEnergy extends OMBlockEntity implements IItemHandler {
    protected ItemStackHandler inventory;
    protected EnergyStorage energyStorage;

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> this);

    private final LazyOptional<IEnergyStorage> energyStorageLazyOptional = LazyOptional.of(() -> energyStorage);

    public BlockEntityStorageEnergy(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, EnergyStorage energyStorage) {
        super(pType, pPos, pBlockState);
        this.energyStorage = energyStorage;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        this.energyStorage.deserializeNBT(tag.get("energyStorage"));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.put("energyStorage", this.energyStorage.serializeNBT());
        return tag;
    }

    @Override
    public abstract int getSlots();

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (inventory.getSlots() > slot) {
            return getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (inventory.getSlots() < slot || !inventory.getStackInSlot(slot).is(stack.getItem())) {
            return stack;
        }
        ItemStack inSlot = getStackInSlot(slot);
        int toInsert = Math.min(getSlotLimit(slot) - inSlot.getCount(), stack.getCount());
        if (!simulate) {
            inSlot.setCount(inSlot.getCount() + toInsert);
        }
        stack.setCount(stack.getCount() - toInsert);
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (inventory.getSlots() < slot) {
            return ItemStack.EMPTY;
        }
        ItemStack inSlot = getStackInSlot(slot);
        ItemStack toExtract = new ItemStack(inSlot.getItem());
        toExtract.setCount(Math.min(amount, inSlot.getCount()));
        if (!simulate) {
            inSlot.setCount(inSlot.getCount() - toExtract.getCount());
        }
        return toExtract;
    }

    @Override
    public abstract int getSlotLimit(int slot);

    @Override
    public abstract boolean isItemValid(int slot, @NotNull ItemStack stack);


    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return energyStorageLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
