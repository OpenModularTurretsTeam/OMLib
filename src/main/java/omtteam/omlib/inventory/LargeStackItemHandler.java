package omtteam.omlib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * An {@link ItemStackHandler} with the slot limits of {@link LargeStackHandler#getSlotLimit(int, ItemStack)}: inserts
 * (hoppers, pipes, containers) fill a slot up to its limit, extracts hand out at most one normal stack (as
 * {@code ItemStackHandler} does), and {@link #serializeNBT} keeps counts above one stack (1.20.1+ does this in the
 * loader adapters of {@code ItemAccess} and in {@code VersionCompat.saveItems}).
 */
public abstract class LargeStackItemHandler extends ItemStackHandler implements LargeStackHandler {
    protected LargeStackItemHandler(int size) {
        super(size);
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return getSlotLimit(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getSlotLimit(slot, getStackInSlot(slot));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        LargeStackNbt.writeLargeCounts(tag.getTagList("Items", Constants.NBT.TAG_COMPOUND), this);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        LargeStackNbt.readLargeCounts(tag.getTagList("Items", Constants.NBT.TAG_COMPOUND), this);
    }
}
