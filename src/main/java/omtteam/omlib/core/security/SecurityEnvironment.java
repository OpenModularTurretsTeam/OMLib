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

    /** Operators may open every owned block and are never treated as strangers. */
    boolean operatorsCanAccessOwnedBlocks();

    /** Trusted players are hurt too when something of the owner accidentally hits them. */
    boolean damageTrustedPlayers();
}
