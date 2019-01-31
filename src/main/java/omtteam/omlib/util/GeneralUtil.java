package omtteam.omlib.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import omtteam.omlib.reference.OMLibNames;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 17.05.2015.
 * This Class
 */
@SuppressWarnings({"deprecation", "unused"})
public class GeneralUtil {
    public static String shiftDetail = TextFormatting.GRAY + safeLocalize(OMLibNames.Localizations.GUI.SHIFT_DETAIL_START)
            + " " + TextFormatting.YELLOW + TextFormatting.ITALIC + safeLocalize(OMLibNames.Localizations.GUI.SHIFT)
            + TextFormatting.RESET + TextFormatting.GRAY + " " + safeLocalize(OMLibNames.Localizations.GUI.SHIFT_DETAIL_END);

    public static Item getItem(String modid, String name) {
        Item item;
        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid + ":" + name));
        return item;
    }

    public static String safeLocalize(String text) {
        if (!I18n.translateToLocal(text).equals(text)) {
            return I18n.translateToLocal(text);
        } else {
            return I18n.translateToFallback(text);
        }
    }

    public static String safeLocalizeBlockName(String text) {
        if (text.contains(":")) {
            text = "tile." + text.split(":")[1] + ".name";
        } else {
            text = "tile." + text + ".name";
        }
        if (!I18n.translateToLocal(text).equals(text)) {
            return I18n.translateToLocal(text);
        } else {
            return I18n.translateToFallback(text);
        }
    }

    @SuppressWarnings("unused")
    public static String getBooleanLocalization(boolean bool) {
        String localization = (bool ? OMLibNames.Localizations.GUI.TRUE : OMLibNames.Localizations.GUI.FALSE);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return I18n.translateToLocal(localization);
        } else {
            return I18n.translateToFallback(localization);
        }
    }

    @SuppressWarnings("unused")
    public static String getColoredBooleanLocalization(boolean bool) {
        String localization = (bool ? OMLibNames.Localizations.GUI.TRUE : OMLibNames.Localizations.GUI.FALSE);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToLocal(localization);
        } else {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToFallback(localization);
        }
    }

    @SuppressWarnings("unused")
    public static String getColoredBooleanLocalizationYesNo(boolean bool) {
        String localization = (bool ? OMLibNames.Localizations.GUI.YES : OMLibNames.Localizations.GUI.NO);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToLocal(localization);
        } else {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToFallback(localization);
        }
    }

    public static String getMachineModeLocalization(EnumMachineMode mode) {
        String localization = (mode == EnumMachineMode.ALWAYS_OFF ? OMLibNames.Localizations.GUI.ALWAYS_OFF
                : mode == EnumMachineMode.ALWAYS_ON ? OMLibNames.Localizations.GUI.ALWAYS_ON
                : mode == EnumMachineMode.INVERTED ? OMLibNames.Localizations.GUI.INVERTED
                : OMLibNames.Localizations.GUI.NONINVERTED);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return I18n.translateToLocal(localization);
        } else {
            return I18n.translateToFallback(localization);
        }
    }

    public static float getFloatFromString(@Nullable String input) {
        if (input == null) {
            return 0.0f;
        }
        return Float.parseFloat(input);
    }
}