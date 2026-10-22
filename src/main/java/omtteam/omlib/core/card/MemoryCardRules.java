package omtteam.omlib.core.card;

import omtteam.omlib.core.security.PlayerInfo;
import omtteam.omlib.core.security.TrustManager;

/**
 * Copying settings of a turret base or camouflage trap to a memory card and writing them to another one, and who may
 * do that (1.20.1+ {@code MemoryCardRules}; 3.x checked nothing).
 * <p>
 * The card holds the trust list and the redstone mode, which are admin settings, and copying reads them, so both
 * directions need the admin access level (the owner always has it). The camouflage part has its own rules, see
 * {@code CamoPermissions}; the base settings are read and written by OMT's {@code BaseCardRules}.
 */
public final class MemoryCardRules {
    private MemoryCardRules() {
    }

    /** Reading the settings of a block onto a card. */
    public static boolean canCopy(TrustManager trust, PlayerInfo player) {
        return trust.isAdmin(player);
    }

    /** Writing the settings of a card to a block. */
    public static boolean canPaste(TrustManager trust, PlayerInfo player) {
        return trust.isAdmin(player);
    }

    /** The trust list of a base or trap as stored on a card. */
    public static MemoryCardData.TrustSettings captureTrust(TrustManager trust) {
        return new MemoryCardData.TrustSettings(trust.getTrustedPlayers());
    }

    /**
     * Writes a trust list to a base or trap, without permission checks. It replaces the existing list, keeps the owner
     * of the target (the owner is never part of the list) and clamps the levels to the ones the target uses.
     */
    public static void applyTrust(MemoryCardData.TrustSettings settings, TrustManager trust) {
        trust.load(trust.getOwner(), settings.trustedPlayers());
    }
}
