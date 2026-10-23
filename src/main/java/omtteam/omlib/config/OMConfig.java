package omtteam.omlib.config;

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
        // TODO: debugLogging is not read anywhere yet; route the mods' debug log output through a helper that checks it
        @Config.Comment("Should OpenModular Mods print out debug messages in log? (Warning, spammy)")
        public boolean debugLogging = false;
        @Config.RequiresMcRestart
        @Config.Comment("Which recipes to use. Valid values: auto, enderio, mekanism, vanilla")
        public String recipes = "auto";
        @Config.Comment("Enable compat for offline mode servers?")
        public boolean offlineModeSupport = false;
    }
}
