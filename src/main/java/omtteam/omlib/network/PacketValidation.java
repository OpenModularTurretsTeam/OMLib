package omtteam.omlib.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import omtteam.omlib.security.TrustHost;

import java.util.Optional;

/**
 * Server-side checks for client-to-server packets. Never trust the client: every handler re-checks distance and
 * permission.
 */
public final class PacketValidation {
    /** The vanilla container distance: squared distance below 64. */
    private static final double MAX_DISTANCE_SQR = 64.0;

    private PacketValidation() {
    }

    /** The block with a trust list at {@code pos}, if it is loaded and the player is close enough to interact with it. */
    public static Optional<TrustHost> findTrustHost(ServerPlayer player, BlockPos pos) {
        if (!player.level().isLoaded(pos)) return Optional.empty();
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) >= MAX_DISTANCE_SQR) return Optional.empty();
        return player.level().getBlockEntity(pos) instanceof TrustHost host ? Optional.of(host) : Optional.empty();
    }

    /** Tells the player that the permission is missing. */
    public static void sendNoPermission(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.omlib.no_permission"));
    }
}
