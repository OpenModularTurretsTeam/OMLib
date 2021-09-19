package omtteam.omlib.api.permission;

import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.GeneralUtil;

/**
 * This enum contains the different access types for determining the source of access.
 */
public enum EnumPlayerAccessType {
    // NONE means player has no relation to the access list
    NONE("none", OMLibNames.Localizations.GUI.ACCCESS_TYPE_NONE),
    // OWNER means player is the owner
    OWNER("owner", OMLibNames.Localizations.GUI.ACCCESS_TYPE_OWNER),
    // TRUSTED means player is trusted player list
    TRUSTED("trusted", OMLibNames.Localizations.GUI.ACCCESS_TYPE_TRUSTED),
    // OP means OP access is active in config and player is OP
    OP("op", OMLibNames.Localizations.GUI.ACCCESS_TYPE_OP);

    private final String name;
    private final String unlocalizedName;

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
