package omtteam.omlib.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import omtteam.omlib.OMLib;

/**
 * Text helpers of the library's screens.
 */
final class ScreenTexts {
    private ScreenTexts() {
    }

    static MutableComponent gui(String key) {
        return Component.translatable("gui." + OMLib.MOD_ID + "." + key);
    }
}
