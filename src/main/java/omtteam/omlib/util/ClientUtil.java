package omtteam.omlib.util;

import net.minecraft.entity.player.EntityPlayer;
import omtteam.omlib.api.gui.GuiParameters;

public class ClientUtil {
    public static void openGuiFromParameters(EntityPlayer player, GuiParameters params) {
        player.openGui(params.getMod(), params.getModGuiId(), params.getWorld(), params.getPosX(), params.getPosY(), params.getPosZ());
    }
}
