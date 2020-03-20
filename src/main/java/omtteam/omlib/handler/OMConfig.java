package omtteam.omlib.handler;

import net.minecraftforge.common.config.Config;
import omtteam.omlib.reference.Reference;

@Config(modid = Reference.MOD_ID, category = "")
public class OMConfig {
    @Config.Name("General")
    public static ConfigGeneral GENERAL = new ConfigGeneral();

    public static class ConfigGeneral {
        @Config.Comment("Can OPs access all owned blocks?")
        public boolean canOPAccessOwnedBlocks = false;
        @Config.Comment("Should some blocks write debug messages on interaction?")
        public boolean doDebugChat = false;
        @Config.RequiresMcRestart
        @Config.Comment("Which recipes to use. Valid values: auto, enderio, mekanism, vanilla")
        public String recipes = "auto";
        @Config.Comment("Enable compat for offline mode servers?")
        public boolean offlineModeSupport = false;
        @Config.Comment("Is EU support (IndustrialCraft 2 Energy) enabled?")
        public boolean EUSupport = true;
        @Config.Comment("How much RF is one EU?")
        public double EUtoRFRatio = 4D;
    }
}
