package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.util.EnumMachineMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Keridos on 27/07/17.
 * This is the base machine with the mode and tank
 */
public abstract class TileEntityMachine extends TileEntityContainerElectric {
    protected boolean active;
    protected boolean redstone = false;
    protected EnumMachineMode mode;
    protected FluidTank tank = new FluidTank(4000);

    public TileEntityMachine() {
        super();
        this.storage = new OMEnergyStorage(10, 10);
        this.active = true;
        this.mode = EnumMachineMode.INVERTED;
    }

    public void toggleMode() {
        if (mode.ordinal() < EnumMachineMode.values().length - 1) {
            mode = EnumMachineMode.values()[mode.ordinal() + 1];
        } else {
            mode = EnumMachineMode.values()[0];
        }
        refreshActive(this.mode);
    }

    protected void refreshActive(EnumMachineMode mode) {
        switch (mode) {
            case INVERTED:
                this.active = !redstone;
                break;
            case NONINVERTED:
                this.active = redstone;
                break;
            case ALWAYS_ON:
                this.active = true;
                break;
            case ALWAYS_OFF:
                this.active = false;
        }
    }

    public EnumMachineMode getMode() {
        return mode;
    }

    public void setMode(EnumMachineMode mode) {
        this.mode = mode;
        this.refreshActive(this.mode);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("active", active);
        tag.setBoolean("redstone", redstone);
        if (this.getTank() != null) {
            NBTTagCompound tank = new NBTTagCompound();
            this.getTank().writeToNBT(tank);
            tag.setTag("tank", tank);
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (this.getTank() != null && tag.hasKey("tank")) {
            this.getTank().readFromNBT(tag.getCompoundTag("tank"));
        }
        this.active = !tag.hasKey("active") || tag.getBoolean("active");
        this.redstone = tag.getBoolean("redstone");
    }

    public boolean isActive() {
        return active;
    }

    public boolean getRedstone() {
        return this.redstone;
    }

    public void setRedstone(boolean redstone) {
        this.redstone = redstone;
        refreshActive(this.mode);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.getTank() != null || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.getTank() != null)
            return (T) this.getCapabilityTank(facing);
        return super.getCapability(capability, facing);
    }

    /**
     * Returns the internal tank of the tile, without restrictions
     *
     * @return internal tank
     */
    @Nullable
    public abstract FluidTank getTank();

    /**
     * Returns the externally "visible" tank of the tile, with proper restrictions
     *
     * @return internal tank
     */
    @Nullable
    public abstract FluidTank getCapabilityTank(EnumFacing facing);
}