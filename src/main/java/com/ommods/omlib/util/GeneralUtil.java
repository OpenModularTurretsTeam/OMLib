package com.ommods.omlib.util;

import com.ommods.omlib.reference.OMLibNames;
import com.ommods.omlib.util.machine.EnumMachineMode;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;

public class GeneralUtil {
    private GeneralUtil() {
    }

    // ---------------------------------------------------------------
    // Localization Methods
    public static String safeLocalize(String text) {
        return I18n.get(text);
    }

    public static String safeLocalizeBlockName(String text) {
        if (text.contains(":")) {
            text = "tile." + text.split(":")[1] + ".name";
        } else {
            text = "tile." + text + ".name";
        }
        return I18n.get(text);
    }

    public static String getBooleanLocalization(boolean bool) {
        String localization = getBooleanDelocalization(bool);
        return I18n.get(localization);
    }

    public static String getColoredBooleanLocalization(boolean bool) {
        String localization = getBooleanDelocalization(bool);

        return getColoredBooleanColor(bool) + I18n.get(localization);
    }

    public static String getColoredBooleanLocalizationYesNo(boolean bool) {
        String localization = getBooleanDelocalizationYesNo(bool);

        return getColoredBooleanColor(bool) + I18n.get(localization);
    }


    public static String getMachineModeLocalization(EnumMachineMode mode) {
        return I18n.get(getMachineModeDelocalization(mode));
    }

    // ---------------------------------------------------------------
    // Delocalized String Methods
    public static String getBooleanDelocalization(boolean bool) {
        return (bool ? OMLibNames.Localizations.GUI.TRUE : OMLibNames.Localizations.GUI.FALSE);
    }

    public static String getBooleanDelocalizationYesNo(boolean bool) {
        return (bool ? OMLibNames.Localizations.GUI.YES : OMLibNames.Localizations.GUI.NO);
    }

    public static String getMachineModeDelocalization(EnumMachineMode mode) {
        if (mode == EnumMachineMode.ALWAYS_OFF) {
            return (OMLibNames.Localizations.GUI.ALWAYS_OFF);
        } else {
            if (mode == EnumMachineMode.ALWAYS_ON) return OMLibNames.Localizations.GUI.ALWAYS_ON;
            return mode == EnumMachineMode.INVERTED ? OMLibNames.Localizations.GUI.INVERTED
                    : OMLibNames.Localizations.GUI.NONINVERTED;
        }
    }
    // ---------------------------------------------------------------

    public static String getColoredBooleanColor(boolean bool) {
        return (bool ? "§2" : "§4");
    }

    public static float getFloatFromString(@Nullable String input) {
        if (input == null) {
            return 0.0f;
        }
        return Float.parseFloat(input);
    }


//    public static String getShiftDetail() {
//        return TextFormatting.GRAY + safeLocalize(OMLibNames.Localizations.GUI.SHIFT_DETAIL_START)
//                + " " + TextFormatting.YELLOW + TextFormatting.ITALIC + safeLocalize(OMLibNames.Localizations.GUI.SHIFT)
//                + TextFormatting.RESET + TextFormatting.GRAY + " " + safeLocalize(OMLibNames.Localizations.GUI.SHIFT_DETAIL_END);
//    }
}
