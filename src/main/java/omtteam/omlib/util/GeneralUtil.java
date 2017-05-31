package omtteam.omlib.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.GameData;
import omtteam.omlib.reference.OMLibNames;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * Created by Keridos on 17.05.2015.
 * This Class
 */
@SuppressWarnings({"deprecation", "unused"})
public class GeneralUtil {
    public static Item getItem(String modid, String name) {
        Item item;
        item = GameData.getItemRegistry().getObject(new ResourceLocation(modid + ":" + name));
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

    public static String getColoredBooleanLocalizationYesNo(boolean bool) {
        String localization = (bool ? OMLibNames.Localizations.GUI.YES : OMLibNames.Localizations.GUI.NO);
        if (!I18n.translateToLocal(localization).equals(localization)) {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToLocal(localization);
        } else {
            return (bool ? "\u00A72" : "\u00A74") + I18n.translateToFallback(localization);
        }
    }

    public static float getFloatFromString(@Nullable String input) {
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex =
                ("[\\x00-\\x20]*" +  // Optional leading "whitespace"
                        "[+-]?(" + // Optional sign character
                        "NaN|" +           // "NaN" string
                        "Infinity|" +      // "Infinity" string

                        // A decimal floating-point string representing a finite positive
                        // number without a leading sign has at most five basic pieces:
                        // Digits . Digits ExponentPart FloatTypeSuffix
                        //
                        // Since this method allows integer-only strings as input
                        // in addition to strings of floating-point literals, the
                        // two sub-patterns below are simplifications of the grammar
                        // productions from section 3.10.2 of
                        // The Java Language Specification.

                        // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                        "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                        "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                        // Hexadecimal strings
                        "((" +
                        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "(\\.)?)|" +

                        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");// Optional trailing "whitespace"

        if (input != null && Pattern.matches(fpRegex, input))
            return Double.valueOf(input).floatValue(); // Will not throw NumberFormatException
        else {
            return 0.0F;
        }
    }
}