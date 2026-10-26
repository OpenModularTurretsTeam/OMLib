package omtteam.omlib.security;

/**
 * The few rules of ownership that a mod decides (usually from its config). Every owned block entity hands its mod's
 * settings to the library through {@link OwnedHost#ownershipSettings()}; the methods are asked each time, so a config
 * that changes at run time is followed.
 */
public interface OwnershipSettings {
    /** Operators may open every owned block and are never treated as strangers. */
    boolean operatorsCanAccessOwnedBlocks();

    /** Trusted players are hurt too when something of the block's owner accidentally hits them. */
    boolean damageTrustedPlayers();

    /** The most blocks one claim, or one use of the ownership wand, walks through connected blocks. */
    int claimSpreadLimit();

    /** Whether the loaders' fake players (machines) may claim blocks by right-clicking them. */
    boolean fakePlayersCanClaim();
}
