package omtteam.omlib.network;

import net.minecraft.server.level.ServerPlayer;
import omtteam.omlib.compat.VersionCompat;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.config.ConfigManager;

/**
 * Sends the server's configs to joining players: one packet for every mod that has a {@link ConfigManager}. The
 * library's loader entrypoints call it from the player-join event. Kept out of {@code config}, so the config does not
 * depend on the packets.
 */
public final class ConfigSync {
    private ConfigSync() {
    }

    /** Sends the server's configs to a joining player; called by the loader's player-join event on the server. */
    public static void onPlayerJoin(ServerPlayer player) {
        // The singleplayer host already uses these configs
        if (VersionCompat.isSingleplayerOwner(player) || !NetworkCompat.canSend(player, OMLibPackets.CONFIG_SYNC)) {
            return;
        }
        for (ConfigManager<?> manager : ConfigManager.all()) {
            NetworkCompat.sendToPlayer(player, OMLibPackets.CONFIG_SYNC, new ConfigSyncPacket(manager.modId(), manager.localJson()));
        }
    }
}
