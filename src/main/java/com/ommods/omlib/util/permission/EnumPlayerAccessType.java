package com.ommods.omlib.util.permission;


import com.ommods.omlib.reference.OMLibNames;
import com.ommods.omlib.util.GeneralUtil;

/**
 * This enum contains the different access types for determining the source of access.
 */
public enum EnumPlayerAccessType {
    NONE("none", OMLibNames.Localizations.GUI.ACCCESS_TYPE_NONE),
    OWNER("owner", OMLibNames.Localizations.GUI.ACCCESS_TYPE_OWNER),
    TRUSTED("trusted", OMLibNames.Localizations.GUI.ACCCESS_TYPE_TRUSTED),
    OP("op", OMLibNames.Localizations.GUI.ACCCESS_TYPE_OP);

    private String name;
    private String unlocalizedName;

    EnumPlayerAccessType(String name, String unlocalizedName) {
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