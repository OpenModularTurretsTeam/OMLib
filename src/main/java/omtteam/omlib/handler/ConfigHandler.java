package omtteam.omlib.handler;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {

    public static boolean offlineModeSupport;
    public static boolean EUSupport;
    public static double EUtoRFRatio;
    public static boolean canOPAccessOwnedBlocks;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static int potentiaToRFRatio;

    public static void init(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        potentiaToRFRatio = config.get("ModCompatibility", "Potentia to RF conversion ratio per essentia", 500).getInt();
        EUSupport = config.get("ModCompatibility", "Can turrets be powered with EU?", true).getBoolean();
        offlineModeSupport = config.get("General", "Enable offline mode support?(warning, makes turrets fairly unsafe)", false).getBoolean();
        canOPAccessOwnedBlocks = config.get("General", "Enable OPs to access all owned blocks.", false).getBoolean();
        EUtoRFRatio = config.get("ModCompatibility", "EU to RF Ratio", 4.0D).getDouble();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
