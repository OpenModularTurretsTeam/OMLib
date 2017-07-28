package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.power.OMEnergyStorage;

/**
 * Created by Keridos on 27/07/17.
 * This Class
 */
public abstract class TileEntityMachine extends TileEntityContainerElectric {
    protected boolean active = false;
    protected boolean redstone = false;
    protected EnumMachineMode mode;

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
        refreshActive();
    }

    private void refreshActive() {
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
        this.refreshActive();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setBoolean("active", active);
        nbtTagCompound.setBoolean("redstone", redstone);
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.active = !nbtTagCompound.hasKey("active") || nbtTagCompound.getBoolean("active");
        this.redstone = nbtTagCompound.getBoolean("redstone");
    }

    public boolean isActive() {
        return active;
    }

    public boolean getRedstone() {
        return this.redstone;
    }

    public void setRedstone(boolean redstone) {
        this.redstone = redstone;
        refreshActive();
    }
}