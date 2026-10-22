package omtteam.omlib.core.security;

import java.util.Optional;
import java.util.UUID;

/**
 * What the permission system needs to know about the world and the config. Implemented by the Minecraft layer.
 */
public interface SecurityEnvironment {
    /**
     * The scoreboard team of a player, looked up by name (works for offline players too).
     */
    Optional<String> getTeam(UUID uuid, String name);

    /** OMLib config {@code canOPAccessOwnedBlocks}: operators may open every block and are never targeted. */
    boolean operatorsCanAccessOwnedBlocks();

    /** OMT config {@code turretDamageTrustedPlayers}: trusted players are hurt when accidentally hit. */
    boolean turretsDamageTrustedPlayers();

    /** OMLib {@code /omlib share}: the owner shares ownership with the player, who then counts as owner. */
    boolean isSharedOwner(PlayerIdentity owner, PlayerIdentity player);

    /**
     * OMLib config {@code offlineModeSupport}: players also match a stored owner or trust list entry by name (ignoring
     * case), as in 3.x.
     */
    boolean offlineModeSupport();
}
