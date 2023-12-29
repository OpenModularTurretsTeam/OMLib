package com.ommods.omlib.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;

public class CommonConfig {

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("DEBUG");
        final ForgeConfigSpec.ConfigValue<Boolean> doDebugChat =
                builder.comment("Should some blocks write debug messages on interaction?")
                        .define("doDebugChat", false);
        final ForgeConfigSpec.ConfigValue<Boolean> debugLogging =
                builder.comment("Should OpenModular Mods print out debug messages in log? (Warning, spammy)")
                        .define("debugLogging", false);
        builder.pop();
    }
}
