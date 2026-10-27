package omtteam.omlib.core.camo;

import omtteam.omlib.core.security.PlayerInfo;
import omtteam.omlib.core.security.TrustManager;

/**
 * Who may change the camouflage (3.x: OMLib {@code handleCamoActivation} was owner-only, the light messages required an
 * admin; since 4.0.0 admins also set the disguise of a turret base).
 */
public final class CamoPermissions {
    private CamoPermissions() {
    }

    /** Setting or resetting the disguise of a turret base: admins (the owner always is one). */
    public static boolean canSetDisguise(TrustManager trust, PlayerInfo player) {
        return trust.isAdmin(player);
    }

    /** Changing light value and opacity of a turret base: admins (the owner always is one). */
    public static boolean canChangeLight(TrustManager trust, PlayerInfo player) {
        return trust.isAdmin(player);
    }

    /**
     * Everything about a camouflage trap (disguise, light, memory cards, trusted players): its admins. The trap only
     * has trusted players and admins, and the owner always is an admin.
     */
    public static boolean canManageTrap(TrustManager trust, PlayerInfo player) {
        return trust.isAdmin(player);
    }
}
