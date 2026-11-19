package omtteam.omlib.util;

import omtteam.omlib.core.machine.MachineMode;
import omtteam.omlib.reference.OMLibNames;

/**
 * This enum contains the different modes our machines can be in, relating to redstone control.
 * The 3.x facade of the core {@link MachineMode} (same constants in the same order, saved as the ordinal).
 */
public enum EnumMachineMode {
    ALWAYS_ON("always_on", OMLibNames.Localizations.GUI.ALWAYS_ON),
    ALWAYS_OFF("always_off", OMLibNames.Localizations.GUI.ALWAYS_OFF),
    INVERTED("inverted", OMLibNames.Localizations.GUI.INVERTED),
    NONINVERTED("noninverted", OMLibNames.Localizations.GUI.NONINVERTED);

    private final String name;
    private final String unlocalizedName;

    EnumMachineMode(String name, String unlocalizedName) {
        this.name = name;
        this.unlocalizedName = unlocalizedName;
    }

    public String getName() {
        return name;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public String getLocalizedName() {
        return GeneralUtil.safeLocalize(unlocalizedName);
    }

    /** The core mode of this constant. */
    public MachineMode toCore() {
        return MachineMode.values()[ordinal()];
    }

    /** The 3.x constant of a core mode. */
    public static EnumMachineMode fromCore(MachineMode mode) {
        return values()[mode.ordinal()];
    }
}

