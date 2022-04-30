package omtteam.omlib.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 05/12/2015.
 * This Class is the abstract class handling Inventory Functions.
 */
@SuppressWarnings("WeakerAccess")
public abstract class TileEntityContainerElectric extends TileEntityElectric {
    @Override
    public NBTTagCompound saveToNBT(NBTTagCompound nbtTagCompound) {
        super.saveToNBT(nbtTagCompound);

        writeInventoryToNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void loadFromNBT(NBTTagCompound nbtTagCompound) {
        super.loadFromNBT(nbtTagCompound);
        readInventoryFromNBT(nbtTagCompound);
    }

    private void readInventoryFromNBT(NBTTagCompound tagCompound) {
        if (tagCompound.getTagId("Items") == Constants.NBT.TAG_LIST) {
            NBTTagList tagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < this.getInventory().getSlots(); i++) {
                this.getInventory().setStackInSlot(i, ItemStack.EMPTY);
            }

            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
                byte slot = tag.getByte("Slot");

                if (slot < this.getInventory().getSlots()) {
                    this.getInventory().setStackInSlot(slot, new ItemStack(tag));
                }
            }

            return;
        } else if (tagCompound.hasKey("Inventory") && // TODO: this should be removed on 1.13
                tagCompound.getCompoundTag("Inventory").getTagId("Items") == Constants.NBT.TAG_LIST) {
            NBTTagList tagList = tagCompound.getCompoundTag("Inventory").getTagList("Items", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < this.getInventory().getSlots(); i++) {
                this.getInventory().setStackInSlot(i, ItemStack.EMPTY);
            }

            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
                byte slot = tag.getByte("Slot");

                if (slot < this.getInventory().getSlots()) {
                    this.getInventory().setStackInSlot(slot, new ItemStack(tag));
                }
            }

            return;
        }

        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(this.getInventory(), null, tagCompound.getTag("Slots"));
    }

    private void writeInventoryToNBT(NBTTagCompound tagCompound) {
        tagCompound.setTag("Items", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(this.getInventory(), null));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.getCapabilityInventory(facing);
        }
        return super.getCapability(capability, facing);
    }

    /**
     * Returns the internal inventory of the item, without restrictions
     *
     * @return internal Inventory
     */
    public abstract IItemHandlerModifiable getInventory();

    /**
     * Returns the externally "visible" inventory of the item, with proper restrictions
     *
     * @return internal Inventory
     */
    public abstract IItemHandler getCapabilityInventory(EnumFacing facing);
}
