package omtteam.omlib.util;

import omtteam.omlib.reference.OMLibNames;

/**
 * This enum contains the different modes our machines can be in, relating to redstone control.
 */
public enum EnumMachineMode {
    ALWAYS_ON("always_on", OMLibNames.Localizations.GUI.ALWAYS_ON),
    ALWAYS_OFF("always_off", OMLibNames.Localizations.GUI.ALWAYS_OFF),
    INVERTED("inverted", OMLibNames.Localizations.GUI.INVERTED),
    NONINVERTED("noninverted", OMLibNames.Localizations.GUI.NONINVERTED);

    private String name;
    private String unlocalizedName;

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
}

