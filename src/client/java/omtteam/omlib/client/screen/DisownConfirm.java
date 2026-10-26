package omtteam.omlib.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import omtteam.omlib.client.compat.ClientNetworkCompat;
import omtteam.omlib.network.OMLibPackets;
import omtteam.omlib.network.TrustPackets;

/**
 * The owner's "Disown" button for a block's screen, with its confirmation. The server
 * checks ownership again; open menus of the block close themselves once nobody owns it.
 */
public final class DisownConfirm {
    private DisownConfirm() {
    }

    /** A red "Disown" button that asks before disowning the block at {@code pos}; "No" runs {@code onCancel}. */
    public static Button button(int x, int y, int width, BlockPos pos, Runnable onCancel) {
        return Button.builder(ScreenTexts.gui("disown").withStyle(ChatFormatting.RED), b -> ask(pos, onCancel))
                .bounds(x, y, width, 20)
                .tooltip(Tooltip.create(Component.translatable("tooltip.omlib.disown")))
                .build();
    }

    private static void ask(BlockPos pos, Runnable onCancel) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                ClientNetworkCompat.sendToServer(OMLibPackets.DISOWN, new TrustPackets.Disown(pos));
                minecraft.setScreen(null);
            } else {
                onCancel.run();
            }
        }, Component.translatable("gui.omlib.disown.title"),
                Component.translatable("gui.omlib.disown.warning")));
    }
}
