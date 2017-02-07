package omtteam.omlib.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import omtteam.omlib.compatability.minecraft.CompatSidedInventory;
import omtteam.omlib.util.compat.ItemStackList;
import omtteam.omlib.util.compat.ItemStackTools;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.util.compat.ItemStackTools.loadFromNBT;
import static omtteam.omlib.util.compat.ItemStackTools.setStackSize;

/**
 * Created by Keridos on 05/12/2015.
 * This Class is the abstract class handling SidedInventory.
 */
@SuppressWarnings("WeakerAccess")
public abstract class TileEntityContainer extends TileEntityOwnedBlock implements CompatSidedInventory {
    protected ItemStackList inventory = ItemStackList.create();

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.getStackInSlot(i);

            if (stack != ItemStackTools.getEmptyStack()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbtTagCompound.setTag("Inventory", itemList);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        NBTTagList tagList = nbtTagCompound.getTagList("Inventory", 10);

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.size()) {
                inventory.set(slot, loadFromNBT(tag));
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        ItemStack stack = getStackInSlot(slot);

        if (stack != ItemStackTools.getEmptyStack()) {
            if (ItemStackTools.getStackSize(stack) <= amt) {
                setInventorySlotContents(slot, ItemStackTools.getEmptyStack());
            } else {
                stack = stack.splitStack(amt);
                if (ItemStackTools.getStackSize(stack) == 0) {
                    setInventorySlotContents(slot, ItemStackTools.getEmptyStack());
                }
            }
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack != ItemStackTools.getEmptyStack() && ItemStackTools.getStackSize(stack) > getInventoryStackLimit()) {
            setStackSize(stack, getInventoryStackLimit());
        }
    }

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.get(slot);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isUsable(EntityPlayer player) {
        return this.getWorld().getTileEntity(pos) == this && player.getDistanceSq(this.pos.getX() + 0.5,
                this.pos.getY() + 0.5,
                this.pos.getZ() + 0.5) < 64;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isItemValidForSlot(int slot, @Nullable ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack itemstack = getStackInSlot(slot);
        setInventorySlotContents(slot, ItemStackTools.getEmptyStack());
        return itemstack;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getName() {
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void openInventory(EntityPlayer player) {

    }

    @Override
    @ParametersAreNonnullByDefault
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void clear() {

    }
}
