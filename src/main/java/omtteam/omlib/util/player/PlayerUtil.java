package omtteam.omlib.util.player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.api.permission.EnumAccessLevel;
import omtteam.omlib.api.permission.EnumPlayerAccessType;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.api.permission.ITrustedPlayersManager;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.PlayerInfo;
import omtteam.omlib.core.security.TrustManager;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Keridos on 20/07/16.
 * A lib for all player based functions.
 */

public class PlayerUtil {
    @Nullable
    public static IHasTrustManager getTrustMachine(IHasOwner tile) {
        if (tile instanceof IHasTrustManager) {
            return (IHasTrustManager) tile;
        }
        return null;
    }

    @Nullable
    public static TrustedPlayer getTrustedPlayer(EntityPlayer player, IHasOwner tile) {
        return getTrustedPlayer(new Player(player), tile);
    }

    @Nullable
    public static TrustedPlayer getTrustedPlayer(Player player, IHasOwner tile) {
        ITrustedPlayersManager manager = trustManager(tile);
        return manager != null ? manager.getTrustedPlayer(player) : null;
    }

    @ParametersAreNullableByDefault
    public static boolean isPlayerOP(EntityPlayer player) {
        if (player != null) {
            return player.canUseCommand(4, "");
        }
        return false;
    }

    @ParametersAreNullableByDefault
    public static boolean isPlayerOP(Player player) {
        if (player != null) {
            EntityPlayer entityPlayer = player.getEntityPlayer();
            return isPlayerOP(entityPlayer);
        }
        return false;
    }

    @Nullable
    public static UUID getPlayerUUID(String username) {
        for (Map.Entry<UUID, String> entry : UsernameCache.getMap().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(username)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    @ParametersAreNullableByDefault
    public static UUID getPlayerUIDUnstable(String possibleUUID) {
        if (possibleUUID == null || possibleUUID.isEmpty()) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(possibleUUID);
        } catch (IllegalArgumentException e) {
            uuid = getPlayerUUID(possibleUUID);
        }
        return uuid;
    }

    @Nullable
    @ParametersAreNullableByDefault
    public static String getPlayerNameFromUUID(String possibleUUID) {
        if (possibleUUID == null || possibleUUID.isEmpty()) {
            return null;
        }
        return UsernameCache.getLastKnownUsername(UUID.fromString(possibleUUID));
    }

    // ---- permission checks: forwarded to the core TrustManager of the block ----

    /**
     * @param entityPlayer the player to check
     * @param ownedBlock   the block to check against
     * @return the type of the players access to the block sorted by owner > trusted > op > none
     */
    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerAccessType(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        return getPlayerAccessType(info(entityPlayer), ownedBlock);
    }

    /**
     * @param player the player to check
     * @param block  the block to check against
     * @return the type of the players access to the block sorted by owner > trusted > op > none
     */
    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerAccessType(Player player, Object block) {
        return getPlayerAccessType(info(player), block);
    }

    private static EnumPlayerAccessType getPlayerAccessType(@Nullable PlayerInfo player, Object block) {
        TrustManager manager = block instanceof IHasOwner ? core((IHasOwner) block) : null;
        if (player != null && manager != null) {
            if (manager.isOwner(player)) {
                return EnumPlayerAccessType.OWNER;
            }
            if (manager.getTrustLevel(player.identity()).isPresent()) {
                return EnumPlayerAccessType.TRUSTED;
            }
        }
        if (player != null && player.operator() && OMConfig.GENERAL.canOPAccessOwnedBlocks) {
            return EnumPlayerAccessType.OP;
        }
        return EnumPlayerAccessType.NONE;
    }

    /** Whether the player is trusted at any level: on the trust list or in the owner's scoreboard team. */
    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(EntityPlayer entityPlayer, IHasOwner ownedBlock) {
        return isTrusted(info(entityPlayer), ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(Player player, IHasOwner ownedBlock) {
        return isTrusted(info(player), ownedBlock);
    }

    private static boolean isTrusted(@Nullable PlayerInfo player, IHasOwner ownedBlock) {
        TrustManager manager = core(ownedBlock);
        return player != null && manager != null && manager.getTrustLevel(player.identity()).isPresent();
    }

    /** The owner or a player the owner shares ownership with ({@code /omlib share}). */
    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(EntityPlayer checkPlayer, IHasOwner ownedBlock) {
        return isOwner(info(checkPlayer), ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(Player player, IHasOwner ownedBlock) {
        return isOwner(info(player), ownedBlock);
    }

    private static boolean isOwner(@Nullable PlayerInfo player, IHasOwner ownedBlock) {
        TrustManager manager = core(ownedBlock);
        return player != null && manager != null && manager.isOwner(player);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(EntityPlayer checkPlayer, IHasOwner machine) {
        return hasAccess(info(checkPlayer), machine, AccessLevel.ADMIN);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(Player player, IHasOwner ownedBlock) {
        return hasAccess(info(player), ownedBlock, AccessLevel.ADMIN);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(EntityPlayer checkPlayer, IHasOwner machine) {
        return hasAccess(info(checkPlayer), machine, AccessLevel.CHANGE_SETTINGS);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(Player player, IHasOwner ownedBlock) {
        return hasAccess(info(player), ownedBlock, AccessLevel.CHANGE_SETTINGS);
    }

    // Use this for block access.
    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(EntityPlayer entityPlayer, IHasOwner ownedBlock) {
        return hasAccess(info(entityPlayer), ownedBlock, AccessLevel.OPEN_GUI);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(Player player, IHasOwner ownedBlock) {
        return hasAccess(info(player), ownedBlock, AccessLevel.OPEN_GUI);
    }

    private static boolean hasAccess(@Nullable PlayerInfo player, IHasOwner ownedBlock, AccessLevel required) {
        TrustManager manager = core(ownedBlock);
        return player != null && manager != null && manager.hasAccess(player, required);
    }

    @ParametersAreNonnullByDefault
    public static void addChatMessage(ICommandSender sender, ITextComponent component) {
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }

    /** The effective access level; {@link EnumAccessLevel#NONE} also for players without any relation. */
    @ParametersAreNonnullByDefault
    public static EnumAccessLevel getPlayerAccessLevel(EntityPlayer player, IHasOwner ownedBlock) {
        TrustManager manager = core(ownedBlock);
        if (manager == null) {
            return EnumAccessLevel.NONE;
        }
        Optional<AccessLevel> level = manager.getAccessLevel(info(player));
        return level.isPresent() ? EnumAccessLevel.fromCore(level.get()) : EnumAccessLevel.NONE;
    }

    /** The trust list facade of a block, or of the facade passed in directly (some callers do that). */
    @Nullable
    private static ITrustedPlayersManager trustManager(@Nullable IHasOwner block) {
        if (block instanceof IHasTrustManager) {
            return ((IHasTrustManager) block).getTrustManager();
        }
        return block instanceof ITrustedPlayersManager ? (ITrustedPlayersManager) block : null;
    }

    /**
     * The core trust manager of a block. Its owner comes from the block if the trust list has none (blocks that keep
     * the owner outside, as 3.x allowed), and a block without a trust list gets a temporary one with only its owner,
     * so owner, shared owner and operator rules still apply; {@code null} if there is no owner at all.
     */
    @Nullable
    private static TrustManager core(IHasOwner block) {
        ITrustedPlayersManager manager = trustManager(block);
        PlayerIdentity blockOwner = OMLibSecurityEnvironment.toIdentity(block.getOwner());
        if (manager != null && (manager.core().getOwner() != null || blockOwner == null)) {
            return manager.core();
        }
        if (blockOwner == null) {
            return null;
        }
        List<omtteam.omlib.core.security.TrustedPlayer> entries = new ArrayList<>();
        if (manager != null) {
            for (omtteam.omlib.core.security.TrustedPlayer entry : manager.core().getTrustedPlayers()) {
                entries.add(entry.copy());
            }
        }
        TrustManager temporary = new TrustManager(OMLibSecurityEnvironment.INSTANCE, () -> {
        });
        temporary.load(blockOwner, entries);
        return temporary;
    }

    private static PlayerInfo info(EntityPlayer player) {
        return new PlayerInfo(player.getUniqueID(), player.getName(), isPlayerOP(player));
    }

    /**
     * {@code null} without a name; the operator flag is only looked up when operators have access (it needs the
     * player online on this server).
     */
    @Nullable
    private static PlayerInfo info(Player player) {
        PlayerIdentity identity = OMLibSecurityEnvironment.toIdentity(player);
        if (identity == null) {
            return null;
        }
        boolean operator = OMConfig.GENERAL.canOPAccessOwnedBlocks && OMLibSecurityEnvironment.server() != null
                && isPlayerOP(player);
        return new PlayerInfo(identity.uuid(), identity.name(), operator);
    }

    @Nullable
    public static Player getPlayerFromUsernameCache(String name) {
        Player player = null;
        for (Map.Entry<UUID, String> serverName : UsernameCache.getMap().entrySet()) {
            if (name.equals(serverName.getValue())) {
                player = new Player(serverName.getKey(), serverName.getValue());
                break;
            }
        }
        return player;
    }
}
