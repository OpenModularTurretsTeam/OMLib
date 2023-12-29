package com.ommods.omlib.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class OMConfig {
    public static ForgeConfigSpec serverConfigSpec;
    public static ForgeConfigSpec commonConfigSpec;



    public static class General {
        public static String recipes;
    }

    public static class Permission {
        public static Boolean canOPAccessOwnedBlocks;
        public static Boolean offlineModeSupport;
    }

    public static class Debug {

        public static Boolean doDebugChat;
        public static Boolean debugLogging;

    }


    static {
        Pair<ServerConfig, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder()
                .configure(ServerConfig::new);
        Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                .configure(CommonConfig::new);


        // Store pair values in some constant field
        serverConfigSpec = serverPair.getValue();
        commonConfigSpec = commonPair.getValue();

        General.recipes = serverConfigSpec.get("general.recipes");
        Permission.canOPAccessOwnedBlocks = serverConfigSpec.get("canOPAccessOwnedBlocks");
        Permission.offlineModeSupport = serverConfigSpec.get("offlineModeSupport");

        Debug.doDebugChat = commonConfigSpec.get("doDebugChat");
        Debug.debugLogging = commonConfigSpec.get("debugLogging");

    }
}
