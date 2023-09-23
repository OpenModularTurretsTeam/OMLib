package omtteam.omlib.api.permission;

import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.GeneralUtil;

/**
 * This enum contains the different trust levels for trusted players.
 */
public enum EnumAccessLevel {
    NONE("none", OMLibNames.Localizations.GUI.ACCESS_LEVEL_NONE),
    OPEN_GUI("open_gui", OMLibNames.Localizations.GUI.ACCESS_LEVEL_OPEN_GUI),
    CHANGE_SETTINGS("change_settings", OMLibNames.Localizations.GUI.ACCESS_LEVEL_CHANGE_SETTINGS),
    ADMIN("admin", OMLibNames.Localizations.GUI.ACCESS_LEVEL_ADMIN);

    private final String name;
    private final String unlocalizedName;

    EnumAccessLevel(String name, String unlocalizedName) {
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
