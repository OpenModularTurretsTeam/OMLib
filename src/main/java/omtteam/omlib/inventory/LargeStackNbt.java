package omtteam.omlib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Counts above one stack in saved slot lists (1.20.1+ {@code VersionCompat.saveItems} / {@code loadItems}): vanilla
 * saves a stack's count as a byte, so such a stack is written with count 1 and its entry gets the real count under
 * {@value #LARGE_COUNT_KEY}. A list read without the key loses the extra items but stays loadable.
 * The entries are those of {@code ItemStackHandler} and of the item handler capability (an int or byte "Slot" each).
 */
public final class LargeStackNbt {
    /** Entry key of the real count of a stack above its max stack size; part of the mods' saves. */
    public static final String LARGE_COUNT_KEY = "OMLibCount";

    private LargeStackNbt() {
    }

    /** After {@code items} was written from {@code inventory}: moves the counts of stacks above one stack. */
    public static void writeLargeCounts(NBTTagList items, IItemHandler inventory) {
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound entry = items.getCompoundTagAt(i);
            int slot = savedSlot(entry);
            if (slot < inventory.getSlots() && isLargeStack(inventory.getStackInSlot(slot))) {
                entry.setByte("Count", (byte) 1);
                entry.setInteger(LARGE_COUNT_KEY, inventory.getStackInSlot(slot).getCount());
            }
        }
    }

    /** After {@code inventory} was read from {@code items}: gives the stacks their real counts back. */
    public static void readLargeCounts(NBTTagList items, IItemHandlerModifiable inventory) {
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound entry = items.getCompoundTagAt(i);
            int slot = savedSlot(entry);
            if (slot < inventory.getSlots() && entry.hasKey(LARGE_COUNT_KEY, Constants.NBT.TAG_ANY_NUMERIC)) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    stack.setCount(Math.max(1, entry.getInteger(LARGE_COUNT_KEY)));
                    inventory.setStackInSlot(slot, stack);
                }
            }
        }
    }

    /** The real count of a saved stack entry ({@link #LARGE_COUNT_KEY}), or the count of {@code stack}. */
    public static ItemStack withLargeCount(NBTTagCompound entry, ItemStack stack) {
        if (!stack.isEmpty() && entry.hasKey(LARGE_COUNT_KEY, Constants.NBT.TAG_ANY_NUMERIC)) {
            stack.setCount(Math.max(1, entry.getInteger(LARGE_COUNT_KEY)));
        }
        return stack;
    }

    public static boolean isLargeStack(ItemStack stack) {
        return stack.getCount() > stack.getMaxStackSize();
    }

    /** The slot of an entry (an unsigned byte in vanilla lists, an int in item handler lists). */
    private static int savedSlot(NBTTagCompound entry) {
        return entry.getInteger("Slot") & 0xFF;
    }
}
