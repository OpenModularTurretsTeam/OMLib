package omtteam.omlib.security;

import net.minecraft.world.entity.player.Player;
import omtteam.omlib.compat.VersionCompat;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.PlayerInfo;

/**
 * Converts Minecraft players into the core permission types.
 */
public final class PlayerAdapter {
    private PlayerAdapter() {
    }

    public static PlayerIdentity identity(Player player) {
        return new PlayerIdentity(player.getUUID(), VersionCompat.profileName(player.getGameProfile()));
    }

    public static PlayerInfo info(Player player) {
        return new PlayerInfo(player.getUUID(), VersionCompat.profileName(player.getGameProfile()),
                VersionCompat.isOwnerOperator(player));
    }
}
