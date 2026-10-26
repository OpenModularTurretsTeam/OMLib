package omtteam.omlib.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import omtteam.omlib.camo.CamoHost;
import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.menu.CamoMenus;

/**
 * Server handlers for {@link CamoPackets}. The player must be close, the host must have adjustable light and the
 * player must be allowed to change it.
 */
public final class CamoPacketHandler {
    /** The distance limit of the menus: squared distance below 64. */
    private static final double MAX_DISTANCE_SQR = 64.0;

    private CamoPacketHandler() {
    }

    /** Whoever may use the menu gets it; {@link CamoMenus#open} checks that. */
    public static void handleOpenMenu(CamoPackets.OpenMenu packet, ServerPlayer player) {
        if (!player.level().isLoaded(packet.pos())
                || player.distanceToSqr(Vec3.atCenterOf(packet.pos())) >= MAX_DISTANCE_SQR) {
            return;
        }
        if (player.level().getBlockEntity(packet.pos()) instanceof CamoHost host) {
            CamoMenus.open(player, host);
        }
    }

    /** Back to the menu this one was opened from; that menu checks the permission itself. */
    public static void handleBack(CamoPackets.Back packet, ServerPlayer player) {
        if (!player.level().isLoaded(packet.pos())
                || player.distanceToSqr(Vec3.atCenterOf(packet.pos())) >= MAX_DISTANCE_SQR) {
            return;
        }
        if (player.level().getBlockEntity(packet.pos()) instanceof CamoHost host && host.hasParentMenu()) {
            host.openParentMenu(player);
        }
    }

    public static void handleSetLight(CamoPackets.SetLight packet, ServerPlayer player) {
        if (!player.level().isLoaded(packet.pos())
                || player.distanceToSqr(Vec3.atCenterOf(packet.pos())) >= MAX_DISTANCE_SQR
                || !(player.level().getBlockEntity(packet.pos()) instanceof CamoHost host)
                || !host.isCamoEnabled() || !host.supportsLight()) {
            return;
        }
        if (!host.canChangeLight(player)) {
            player.sendSystemMessage(Component.translatable("message.omlib.no_permission"));
            return;
        }
        host.setLight(new CamoLight(packet.value(), packet.opacity()));
    }
}
