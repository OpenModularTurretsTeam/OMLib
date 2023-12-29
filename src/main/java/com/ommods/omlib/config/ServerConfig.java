package com.ommods.omlib.config;


import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;

public class ServerConfig {

    // In some config class
    ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("PERMISSION");
        // Define values here in final fields
        final ForgeConfigSpec.ConfigValue<Boolean> canOPAccessOwnedBlocks =
                builder.comment("Can OPs access all owned blocks?")
                        .define("canOPAccessOwnedBlocks", false);
        final ForgeConfigSpec.ConfigValue<Boolean> offlineModeSupport =
                builder.comment("Enable compat for offline mode servers?")
                        .define("offlineModeSupport", false);
        builder.pop();
        builder.push("GENERAL");
        final ForgeConfigSpec.ConfigValue<String> recipes =
                builder.comment("Which recipes to use. Valid values: auto,vanilla")
                        .defineInList("recipes", "auto", Arrays.asList("auto", "vanilla"));
        builder.pop();
    }
}