package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.TrustedPlayer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
public abstract class TileEntityMachine extends TileEntityContainerElectric implements ITrustedPlayersManager {
    protected boolean active = false;
    protected boolean redstone = false;
    protected EnumMachineMode mode;
    protected List<TrustedPlayer> trustedPlayers;

    public TileEntityMachine() {
        super();
        this.trustedPlayers = new ArrayList<>();
        this.storage = new OMEnergyStorage(10, 10);
        this.active = true;
        this.mode = EnumMachineMode.INVERTED;
    }

    public void toggleMode() {
        if (mode.ordinal() < EnumMachineMode.values().length) {
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

    public static String getModeAsLocString(EnumMachineMode mode) {
        switch (mode) {
            case INVERTED:
                return OMLibNames.Localizations.GUI.INVERTED;
            case NONINVERTED:
                return OMLibNames.Localizations.GUI.NONINVERTED;
            case ALWAYS_ON:
                return OMLibNames.Localizations.GUI.ALWAYS_ON;
            case ALWAYS_OFF:
                return OMLibNames.Localizations.GUI.ALWAYS_OFF;
        }
        return null;
    }

    @Override
    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void setTrustedPlayers(List<TrustedPlayer> list) {
        this.trustedPlayers = list;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setBoolean("active", active);
        nbtTagCompound.setBoolean("redstone", redstone);
        nbtTagCompound.setTag("trustedPlayers", getTrustedPlayersAsNBT());
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.active = !nbtTagCompound.hasKey("active") || nbtTagCompound.getBoolean("active");
        this.redstone = nbtTagCompound.getBoolean("redstone");
        buildTrustedPlayersFromNBT(nbtTagCompound.getTagList("trustedPlayers", 10));
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
