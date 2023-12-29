package com.ommods.omlib.util.machine;

import com.ommods.omlib.reference.OMLibNames;
import com.ommods.omlib.util.GeneralUtil;

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
}