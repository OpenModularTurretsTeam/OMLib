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

    public static boolean mergeItemStackWithStackLimit(ItemStack itemStackExt, int startIndex, int endIndex, boolean reverseDirection, Container container) {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (itemStackExt.isStackable()) {
            while (getStackSize(itemStackExt) > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
                Slot slot = container.inventorySlots.get(i);
                ItemStack itemStackSlot = slot.getStack();

                if (itemStackSlot != ItemStack.EMPTY  && areItemStacksEqual(itemStackExt, itemStackSlot)) {
                    int j = getStackSize(itemStackSlot) + getStackSize(itemStackExt);

                    if (j <= Math.min(itemStackExt.getMaxStackSize(), slot.getItemStackLimit(itemStackExt))) {
                        setStackSize(itemStackExt, 0);
                        setStackSize(itemStackSlot, j);
                        slot.onSlotChanged();
                        flag = true;
                    }else if (getStackSize(itemStackSlot) < Math.min(itemStackExt.getMaxStackSize(), slot.getItemStackLimit(itemStackExt) - getStackSize(itemStackSlot))) {
                        int stackSizeExt = getStackSize(itemStackExt);
                        setStackSize(itemStackExt, getStackSize(itemStackExt) - Math.min(itemStackExt.getMaxStackSize(), slot.getItemStackLimit(itemStackExt) - getStackSize(itemStackSlot)));
                        setStackSize(itemStackSlot, Math.min(slot.getItemStackLimit(itemStackExt), getStackSize(itemStackSlot) + (stackSizeExt - getStackSize(itemStackExt))));
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

        if (getStackSize(itemStackExt) > 0) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
                Slot slot1 = container.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1 == ItemStack.EMPTY && slot1.isItemValid(itemStackExt)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    ItemStack itemStackToPut = new ItemStack(itemStackExt.getItem(), Math.min(slot1.getItemStackLimit(itemStackExt), getStackSize(itemStackExt)), itemStackExt.getItemDamage());
                    setStackSize(itemStackExt, getStackSize(itemStackExt) - getStackSize(itemStackToPut));
                    slot1.putStack(itemStackToPut);
                    slot1.onSlotChanged();
                    if (getStackSize(itemStackExt) == 0) {
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
