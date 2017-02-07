package omtteam.omlib.util;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import omtteam.omlib.util.compat.ItemStackTools;

import static omtteam.omlib.util.compat.ItemStackTools.getStackSize;
import static omtteam.omlib.util.compat.ItemStackTools.setStackSize;

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
                ItemStack itemstack = slot.getStack();

                if (itemstack != ItemStackTools.getEmptyStack() && areItemStacksEqual(stack, itemstack)) {
                    int j = getStackSize(itemstack) + getStackSize(stack);

                    if (j <= Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack))) {
                        setStackSize(stack, 0);
                        setStackSize(itemstack, j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (getStackSize(itemstack) < Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack) - getStackSize(itemstack))) {
                        setStackSize(stack, getStackSize(itemstack) - Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack) - getStackSize(itemstack)));
                        setStackSize(itemstack, Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack) - getStackSize(itemstack)));
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

                if (itemstack1 == ItemStackTools.getEmptyStack() && slot1.isItemValid(stack)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    ItemStack itemStackToPut = new ItemStack(stack.getItem(), Math.min(slot1.getItemStackLimit(stack), getStackSize(stack)), stack.getItemDamage());
                    setStackSize(stack, getStackSize(stack) - getStackSize(itemStackToPut));
                    slot1.putStack(itemStackToPut);
                    slot1.onSlotChanged();
                    if (getStackSize(stack) == 0) {
                        flag = true;
                    }
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
}
