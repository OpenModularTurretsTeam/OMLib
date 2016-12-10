package omtteam.omlib.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.GameData;
import omtteam.omlib.reference.Names;

/**
 * Created by Keridos on 17.05.2015.
 * This Class
 */
public class GeneralUtil {
    public static Item getMinecraftItem(String name) {
        Item item;
        item = GameData.getItemRegistry().getObject(new ResourceLocation("minecraft:" + name));
        return item;
    }

    public static String safeLocalize(String text) {
        if (!I18n.translateToLocal(text).equals(text)) {
            return I18n.translateToLocal(text);
        } else {
            return I18n.translateToFallback(text);
        }
    }

    public static String getBooleanLocalization(boolean bool) {
        String localization = (bool ? Names.Localizations.TRUE : Names.Localizations.FALSE);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return I18n.translateToLocal(localization);
        } else {
            return I18n.translateToFallback(localization);
        }
    }

    public static String getColoredBooleanLocalization(boolean bool) {
        String localization = (bool ? Names.Localizations.TRUE : Names.Localizations.FALSE);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToLocal(localization);
        } else {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToFallback(localization);
        }
    }
    public static String getColoredBooleanLocalizationYesNo(boolean bool) {
        String localization = (bool ? Names.Localizations.YES : Names.Localizations.NO);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToLocal(localization);
        } else {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToFallback(localization);
        }
    }
}