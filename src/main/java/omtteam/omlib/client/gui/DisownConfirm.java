package omtteam.omlib.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.network.MessageDisown;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.util.player.PlayerUtil;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

/**
 * The owner's "Disown" button (OMT's configure screen of a turret base, the camouflage trap's menu) and its confirmation (1.20.1+
 * {@code DisownConfirm}). The server checks ownership again; open menus of the block close themselves once nobody owns
 * it.
 */
@SideOnly(Side.CLIENT)
public final class DisownConfirm {
    private DisownConfirm() {
    }

    /** Whether the player owns the block, so the button is shown. */
    public static boolean isOwner(EntityPlayer player, IHasTrustManager block) {
        return block.getTrustManager().core().isOwner(PlayerUtil.getPlayerInfo(player));
    }

    /** A red "Disown" button; {@link #ask} it when it is pressed. */
    public static GuiButton button(int id, int x, int y, int width) {
        return new GuiButton(id, x, y, width, 20, TextFormatting.RED + safeLocalize("gui.omlib.disown"));
    }

    /** Asks before disowning the block at {@code pos}; "No" runs {@code onCancel}. */
    public static void ask(BlockPos pos, Runnable onCancel) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.displayGuiScreen(new GuiYesNo((confirmed, id) -> {
            if (confirmed) {
                OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageDisown(pos));
                minecraft.displayGuiScreen(null);
            } else {
                onCancel.run();
            }
        }, safeLocalize("gui.omlib.disown.title"), safeLocalize("gui.omlib.disown.warning"), 0));
    }
}
