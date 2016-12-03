package omtteam.omlib.util;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.logging.Logger;

/**
 * Created by Keridos on 03/12/16.
 * This Class contains a modified version of the mergeItemStack so that it respect slot.getItemStackLimit(stack) and
 * the retuired areItemStacksEqual
 */
public class InvUtil {

    public static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB)
    {
        return stackB.getItem() == stackA.getItem() && (!stackA.getHasSubtypes() || stackA.getMetadata() == stackB.getMetadata()) && ItemStack.areItemStackTagsEqual(stackA, stackB);
    }

    public static boolean mergeItemStackWithStackLimit(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, Container container)
    {
        boolean flag = false;
        int i = startIndex;

        if (reverseDirection)
        {
            i = endIndex - 1;
        }

        if (stack.isStackable())
        {
            while (stack.stackSize > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex))
            {
                Slot slot = (Slot)container.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (itemstack != null && areItemStacksEqual(stack, itemstack))
                {
                    int j = itemstack.stackSize + stack.stackSize;

                    Logger.getGlobal().info (" stacklimit: "+slot.getItemStackLimit(stack));
                    if (j <= Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack)))
                    {
                        stack.stackSize = 0;
                        itemstack.stackSize = j;
                        slot.onSlotChanged();
                        flag = true;
                    }
                    else if (itemstack.stackSize < Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack) - itemstack.stackSize))
                    {
                        stack.stackSize -= Math.min(stack.getMaxStackSize(),slot.getItemStackLimit(stack) - itemstack.stackSize);
                        itemstack.stackSize = Math.min(stack.getMaxStackSize(),slot.getItemStackLimit(stack) - itemstack.stackSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        if (stack.stackSize > 0)
        {
            if (reverseDirection)
            {
                i = endIndex - 1;
            }
            else
            {
                i = startIndex;
            }

            while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)
            {
                Slot slot1 = (Slot)container.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1 == null && slot1.isItemValid(stack)) // Forge: Make sure to respect isItemValid in the slot.
                {
                    ItemStack itemStackToPut = new ItemStack(stack.getItem(),Math.min(slot1.getItemStackLimit(stack), stack.stackSize),stack.getItemDamage());
                    stack.stackSize = stack.stackSize - itemStackToPut.stackSize;
                    slot1.putStack(itemStackToPut);
                    slot1.onSlotChanged();
                    if (stack.stackSize == 0) {
                        flag = true;
                    }
                    break;
                }

                if (reverseDirection)
                {
                    --i;
                }
                else
                {
                    ++i;
                }
            }
        }

        return flag;
    }
}
