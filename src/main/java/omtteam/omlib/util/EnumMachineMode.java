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

    /**
     * The core mode saved as 3.x ordinal (NBT, memory cards), or {@code fallback} if the value is out of range. The
     * ordinal is a 1.12 save format, so this lives here and not in the shared core.
     */
    public static MachineMode byOrdinal(int ordinal, MachineMode fallback) {
        EnumMachineMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal].toCore() : fallback;
    }
}

