package omtteam.omlib.util.player;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.SecurityEnvironment;
import omtteam.omlib.handler.OMConfig;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@link SecurityEnvironment} of OMLib's owned blocks: scoreboard teams of the server, the OMLib config and the
 * owner share register ({@code /omlib share}). Turrets are an OMT matter, so turret damage to trusted players is
 * off here; OMT's blocks supply their own value.
 */
public class OMLibSecurityEnvironment implements SecurityEnvironment {
    public static final OMLibSecurityEnvironment INSTANCE = new OMLibSecurityEnvironment();

    protected OMLibSecurityEnvironment() {
    }

    /** Without a server (on a multiplayer client) nobody has a team. */
    @Override
    public Optional<String> getTeam(UUID uuid, String name) {
        MinecraftServer server = server();
        if (server == null) {
            return Optional.empty();
        }
        ScorePlayerTeam team = server.getWorld(0).getScoreboard().getPlayersTeam(name);
        return team == null ? Optional.<String>empty() : Optional.of(team.getName());
    }

    @Override
    public boolean operatorsCanAccessOwnedBlocks() {
        return OMConfig.GENERAL.canOPAccessOwnedBlocks;
    }

    @Override
    public boolean turretsDamageTrustedPlayers() {
        return false;
    }

    @Override
    public boolean isSharedOwner(PlayerIdentity owner, PlayerIdentity player) {
        return OwnerShareRegister.instance.isPlayerSharedOwner(toPlayer(owner), toPlayer(player));
    }

    @Override
    public boolean offlineModeSupport() {
        return OMConfig.GENERAL.offlineModeSupport;
    }

    /** The OMLib player of an identity, without a team (the share register compares UUIDs or names only). */
    public static Player toPlayer(PlayerIdentity identity) {
        return new Player(identity.uuid(), identity.name(), "");
    }

    /**
     * The identity of an OMLib player; {@code null} without a name. A player without UUID (legacy saves, offline
     * mode) gets the UUID an offline server gives that name.
     */
    public static PlayerIdentity toIdentity(Player player) {
        if (player == null || player.getName() == null) {
            return null;
        }
        UUID uuid = player.getUuid() != null ? player.getUuid() : offlineUuid(player.getName());
        return new PlayerIdentity(uuid, player.getName());
    }

    /** The UUID an offline-mode server gives a player name (vanilla {@code EntityPlayer.getOfflineUUID}). */
    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    /** The running server; {@code null} on a multiplayer client and outside the game (unit tests). */
    static MinecraftServer server() {
        return FMLCommonHandler.instance().getSidedDelegate() == null ? null
                : FMLCommonHandler.instance().getMinecraftServerInstance();
    }
}
