package omtteam.omlib.menu;

import net.minecraft.server.level.ServerPlayer;
import omtteam.omlib.camo.CamoHost;
import omtteam.omlib.compat.MenuCompat;

/**
 * Opens the camouflage menu of a {@link CamoHost}.
 */
public final class CamoMenus {
    private CamoMenus() {
    }

    /** The caller checked that the player may use the menu; the menu itself checks every change again. */
    public static void open(ServerPlayer player, CamoHost host) {
        if (!host.isCamoEnabled() || !(host.canSetCamo(player) || host.canChangeLight(player))) return;
        MenuCompat.open(player, player.level().getBlockState(host.getBlockPos()).getBlock().getName(),
                host.getBlockPos(), (containerId, inventory, menuPlayer) -> CamoMenu.server(containerId, inventory, host));
    }
}
