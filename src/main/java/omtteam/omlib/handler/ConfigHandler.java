package omtteam.omlib.handler;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {

    public static boolean offlineModeSupport;
    public static boolean EUSupport;
    public static double EUtoRFRatio;
    private static int potentiaToRFRatio;
    private static int potentiaAddonCapacity;

    public static void init(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        potentiaToRFRatio = config.get("ModCompatibility", "Potentia Addons' RF conversion ratio per 1 essentia",
                                       500).getInt();

        potentiaAddonCapacity = config.get("ModCompatibility", "How much essentia the Potentia Addon can store",
                                           20).getInt();


        EUSupport = config.get("ModCompatability", "Can turrets be powered with EU?", true).getBoolean();
        offlineModeSupport = config.get("ModCompatability", "Enable offline mode support?(warning, makes turrets fairly unsafe)", false).getBoolean();
        EUtoRFRatio = config.get("ModCompatability", "EU to RF Ratio", 4.0D).getDouble();

        if (config.hasChanged()) {
            config.save();
        }
    }
}
