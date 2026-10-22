package omtteam.omlib.client.gui;

import net.minecraft.entity.player.EntityPlayer;

public class ClientUtil {
    public static void openGuiFromParameters(EntityPlayer player, GuiParameters params) {
        player.openGui(params.getMod(), params.getModGuiId(), params.getWorld(), params.getPosX(), params.getPosY(), params.getPosZ());
    }
}
