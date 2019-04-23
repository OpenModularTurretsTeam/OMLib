package omtteam.omlib.util;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.reference.OMLibNames;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 17.05.2015.
 * This Class provides some general utility including translation stuff.
 */
@SuppressWarnings({"unused"})
public class GeneralUtil {
    public static Item getItem(String modid, String name) {
        Item item;
        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid + ":" + name));
        return item;
    }

    @SideOnly(Side.CLIENT)
    public static String safeLocalize(String text) {
        return I18n.format(text);
    }

    @SideOnly(Side.CLIENT)
    public static String safeLocalizeBlockName(String text) {
        if (text.contains(":")) {
            text = "tile." + text.split(":")[1] + ".name";
        } else {
            text = "tile." + text + ".name";
        }
        return I18n.format(text);
    }

    @SideOnly(Side.CLIENT)
    public static String getBooleanLocalization(boolean bool) {
        String localization = getBooleanUnlocalization(bool);
        return I18n.format(localization);
    }

    @SideOnly(Side.CLIENT)
    public static String getColoredBooleanLocalization(boolean bool) {
        String localization = getBooleanUnlocalization(bool);

        return getColoredBooleanColor(bool) + I18n.format(localization);
    }

    @SideOnly(Side.CLIENT)
    public static String getColoredBooleanLocalizationYesNo(boolean bool) {
        String localization = getBooleanUnlocalizationYesNo(bool);

        return getColoredBooleanColor(bool) + I18n.format(localization);
    }

    public static String getBooleanUnlocalization(boolean bool) {
        return (bool ? OMLibNames.Localizations.GUI.TRUE : OMLibNames.Localizations.GUI.FALSE);
    }

    public static String getBooleanUnlocalizationYesNo(boolean bool) {
        return (bool ? OMLibNames.Localizations.GUI.YES : OMLibNames.Localizations.GUI.NO);
    }

    public static String getColoredBooleanColor(boolean bool) {
        return (bool ? "\u00A72" : "\u00A74");
    }

    @SideOnly(Side.CLIENT)
    public static String getMachineModeLocalization(EnumMachineMode mode) {
        return I18n.format(getMachineModeUnLocalization(mode));
    }

    public static String getMachineModeUnLocalization(EnumMachineMode mode) {
        return (mode == EnumMachineMode.ALWAYS_OFF ? OMLibNames.Localizations.GUI.ALWAYS_OFF
                : mode == EnumMachineMode.ALWAYS_ON ? OMLibNames.Localizations.GUI.ALWAYS_ON
                : mode == EnumMachineMode.INVERTED ? OMLibNames.Localizations.GUI.INVERTED
                : OMLibNames.Localizations.GUI.NONINVERTED);
    }

    public static float getFloatFromString(@Nullable String input) {
        if (input == null) {
            return 0.0f;
        }
        return Float.parseFloat(input);
    }

    @SideOnly(Side.CLIENT)
    public static String getShiftDetail() {
        return TextFormatting.GRAY + safeLocalize(OMLibNames.Localizations.GUI.SHIFT_DETAIL_START)
                + " " + TextFormatting.YELLOW + TextFormatting.ITALIC + safeLocalize(OMLibNames.Localizations.GUI.SHIFT)
                + TextFormatting.RESET + TextFormatting.GRAY + " " + safeLocalize(OMLibNames.Localizations.GUI.SHIFT_DETAIL_END);
    }
}