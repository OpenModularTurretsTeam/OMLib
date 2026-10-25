package omtteam.omlib.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Reads a tile entity inventory from the layouts written over the versions:
 * a list under "Items", the same list inside an "Inventory" compound (3.x), or the item handler list under "Slots";
 * writes the "Items" list.
 */
public final class InventoryNbt {
    private InventoryNbt() {
    }

    public static void write(IItemHandler inventory, NBTTagCompound tagCompound) {
        tagCompound.setTag("Items", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inventory, null));
    }

    public static void read(IItemHandlerModifiable inventory, NBTTagCompound tagCompound) {
        if (tagCompound.getTagId("Items") == Constants.NBT.TAG_LIST) {
            readItemList(inventory, tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND));
        } else if (tagCompound.hasKey("Inventory") && // TODO: this should be removed on 1.13
                tagCompound.getCompoundTag("Inventory").getTagId("Items") == Constants.NBT.TAG_LIST) {
            readItemList(inventory, tagCompound.getCompoundTag("Inventory").getTagList("Items", Constants.NBT.TAG_COMPOUND));
        } else if (tagCompound.getTagId("Slots") == Constants.NBT.TAG_LIST) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inventory, null, tagCompound.getTag("Slots"));
        }
    }

    private static void readItemList(IItemHandlerModifiable inventory, NBTTagList tagList) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");

            if (slot >= 0 && slot < inventory.getSlots()) {
                inventory.setStackInSlot(slot, new ItemStack(tag));
            }
        }
    }
}
