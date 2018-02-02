package omtteam.omlib.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import omtteam.omlib.util.InvUtil;
import omtteam.omlib.util.ItemStackList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 05/12/2015.
 * This Class is the abstract class handling SidedInventory.
 */
@SuppressWarnings("WeakerAccess")
public abstract class TileEntityContainer extends TileEntityOwnedBlock implements ISidedInventory {
    protected ItemStackList inventory = ItemStackList.create();

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        NBTTagCompound inv = new NBTTagCompound();
        ItemStackHelper.saveAllItems(inv, inventory);
        nbtTagCompound.setTag("Inventory", inv);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        NBTTagCompound inv = nbtTagCompound.getCompoundTag("Inventory");
        ItemStackHelper.loadAllItems(inv, inventory);
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        ItemStack stack = getStackInSlot(slot);

        if (stack != ItemStack.EMPTY) {
            if (InvUtil.getStackSize(stack) <= amt) {
                setInventorySlotContents(slot, ItemStack.EMPTY);
            } else {
                stack = stack.splitStack(amt);
                if (InvUtil.getStackSize(stack) == 0) {
                    setInventorySlotContents(slot, ItemStack.EMPTY);
                }
            }
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
        inventory.set(slot, stack);
        if (stack != ItemStack.EMPTY && InvUtil.getStackSize(stack) > getInventoryStackLimit()) {
            InvUtil.setStackSize(stack, getInventoryStackLimit());
        }
    }

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.get(slot);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }



    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
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

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack itemstack = getStackInSlot(slot);
        setInventorySlotContents(slot, ItemStack.EMPTY);
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

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, @Nonnull EnumFacing direction) {
        return false;
    }

    @Override
    public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull EnumFacing direction) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
