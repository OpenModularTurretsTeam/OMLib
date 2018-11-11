package omtteam.omlib.util;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 03/12/16.
 * This Class contains a modified version of the mergeItemStack so that it respect slot.getItemStackLimit(stack) and
 * the retuired areItemStacksEqual
 */
@SuppressWarnings({"WeakerAccess"})
public class InvUtil {

    public static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return stackB.getItem() == stackA.getItem() && (!stackA.getHasSubtypes() || stackA.getMetadata() == stackB.getMetadata()) && ItemStack.areItemStackTagsEqual(stackA, stackB);
    }

    public static boolean mergeItemStackWithStackLimit(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, Container container) {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (getStackSize(stack) > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
                Slot slot = container.inventorySlots.get(i);
                ItemStack itemStackSlot = slot.getStack();

                if (!itemStackSlot.isEmpty() && areItemStacksEqual(stack, itemStackSlot) && ItemStack.areItemStackTagsEqual(stack, itemStackSlot)) {
                    int j = getStackSize(itemStackSlot) + getStackSize(stack);

                    if (j <= Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack))) {
                        setStackSize(stack, 0);
                        setStackSize(itemStackSlot, j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (getStackSize(itemStackSlot) <= Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack) - getStackSize(itemStackSlot))) {
                        int stackSizeExt = getStackSize(stack);
                        setStackSize(stack, getStackSize(stack) - Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack) - getStackSize(itemStackSlot)));
                        setStackSize(itemStackSlot, Math.min(slot.getItemStackLimit(stack), getStackSize(itemStackSlot) + (stackSizeExt - getStackSize(stack))));
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (getStackSize(stack) > 0) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
                Slot slot1 = container.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1.isEmpty() && slot1.isItemValid(stack)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    if (stack.getCount() > slot1.getItemStackLimit(stack)) {
                        slot1.putStack(stack.splitStack(slot1.getItemStackLimit(stack)));
                    } else {
                        slot1.putStack(stack.splitStack(stack.getCount()));
                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    public static int getStackSize(@Nonnull ItemStack stack) {
        return stack.getCount();
    }

    @Nonnull
    public static ItemStack setStackSize(@Nonnull ItemStack stack, int amount) {
        if (amount <= 0) {
            stack.setCount(0);
            return ItemStack.EMPTY;
        }
        stack.setCount(amount);
        return stack;
    }
}
