package omtteam.omlib.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import omtteam.omlib.inventory.CompatSidedInventory;
import omtteam.omlib.util.ItemStackUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.util.ItemStackUtil.loadFromNBT;
import static omtteam.omlib.util.ItemStackUtil.setStackSize;

/**
 * Created by Keridos on 05/12/2015.
 * This Class is the abstract class handling SidedInventory.
 */
@SuppressWarnings("WeakerAccess")
public abstract class TileEntityContainer extends TileEntityOwnedBlock implements CompatSidedInventory {
    protected ItemStack[] inventory;

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < this.inventory.length; i++) {
            ItemStack stack = this.getStackInSlot(i);

            if (stack != ItemStackUtil.getEmptyStack()) {
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
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = loadFromNBT(tag);
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        ItemStack stack = getStackInSlot(slot);

        if (stack != ItemStackUtil.getEmptyStack()) {
            if (stack.stackSize <= amt) {
                setInventorySlotContents(slot, ItemStackUtil.getEmptyStack());
            } else {
                stack = stack.splitStack(amt);
                if (stack.stackSize == 0) {
                    setInventorySlotContents(slot, ItemStackUtil.getEmptyStack());
                }
            }
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != ItemStackUtil.getEmptyStack() && ItemStackUtil.getStackSize(stack) > getInventoryStackLimit()) {
            setStackSize(stack, getInventoryStackLimit());
        }
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq(this.pos.getX()  + 0.5,
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
        setInventorySlotContents(slot, ItemStackUtil.getEmptyStack());
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
