package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 05/12/2015.
 * This Class is the abstract class handling Inventory Functions.
 */
@SuppressWarnings("WeakerAccess")
public abstract class TileEntityContainer extends TileEntityOwnedBlock { // TODO: Strategy Pattern for Features
    @Override
    public NBTTagCompound saveToNBT(NBTTagCompound nbtTagCompound) {
        super.saveToNBT(nbtTagCompound);

        NBTTagCompound inv = new NBTTagCompound();
        writeInventoryToNBT(nbtTagCompound);
        nbtTagCompound.setTag("Inventory", inv);
        return nbtTagCompound;
    }

    @Override
    public void loadFromNBT(NBTTagCompound nbtTagCompound) {
        super.loadFromNBT(nbtTagCompound);
        InventoryNbt.read(this.getInventory(), nbtTagCompound);
    }

    private void writeInventoryToNBT(NBTTagCompound tagCompound) {
        tagCompound.setTag("Slots", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(this.getInventory(), null));
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
    public abstract RangedWrapper getCapabilityInventory(EnumFacing facing);
}
