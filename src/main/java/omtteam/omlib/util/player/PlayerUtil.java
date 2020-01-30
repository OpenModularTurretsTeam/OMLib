package omtteam.omlib.util.player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.api.permission.*;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.Map;
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
        if (tile instanceof IHasTrustManager) {
            return ((IHasTrustManager) tile).getTrustManager().getTrustedPlayer(player);
        }
        return null;
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

    /**
     * @param entityPlayer the player to check
     * @param ownedBlock   the block to check against
     * @return the type of the players access to the block sorted by owner > trusted > op > none
     */
    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerAccessType(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(entityPlayer);
        return getPlayerAccessType(player, ownedBlock);
    }

    /**
     * @param player the player to check
     * @param block  the block to check against
     * @return the type of the players access to the block sorted by owner > trusted > op > none
     */
    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerAccessType(Player player, Object block) {
        IHasOwner ownedBlock = null;
        if (block instanceof IHasOwner) {
            ownedBlock = (IHasOwner) block;
        }
        if (ownedBlock != null) {
            Player owner = ownedBlock.getOwner();
            if (player.equals(owner) || OwnerShareRegister.instance.isPlayerSharedOwner(owner, player)) {
                return EnumPlayerAccessType.OWNER;
            }
        }
        if (getTrustedPlayer(player, ownedBlock) != null) {
            return EnumPlayerAccessType.TRUSTED;
        } else if (OMConfig.GENERAL.canOPAccessOwnedBlocks && isPlayerOP(player)) {
            return EnumPlayerAccessType.OP;
        }
        return EnumPlayerAccessType.NONE;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(EntityPlayer entityPlayer, IHasOwner ownedBlock) {
        Player player = new Player(entityPlayer);
        return isPlayerTrusted(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(Player player, IHasOwner ownedBlock) {
        return getTrustedPlayer(player, ownedBlock) != null;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(EntityPlayer checkPlayer, IHasOwner ownedBlock) {
        Player player = new Player(checkPlayer);
        return isPlayerOwner(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(Player player, IHasOwner ownedBlock) {
        return player.equals(ownedBlock.getOwner())
                || OwnerShareRegister.instance.isPlayerSharedOwner(ownedBlock.getOwner(), player);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(EntityPlayer checkPlayer, IHasOwner machine) {
        Player player = new Player(checkPlayer);
        return isPlayerAdmin(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(Player player, IHasOwner ownedBlock) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, ownedBlock);
        return isPlayerOwner(player, ownedBlock) || (trustedPlayer != null &&
                trustedPlayer.getAccessLevel().equals(EnumAccessLevel.ADMIN));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(EntityPlayer checkPlayer, IHasOwner machine) {
        Player player = new Player(checkPlayer);
        return canPlayerChangeSetting(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(Player player, IHasOwner ownedBlock) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, ownedBlock);
        return isPlayerAdmin(player, ownedBlock) || (trustedPlayer != null &&
                trustedPlayer.getAccessLevel().equals(EnumAccessLevel.CHANGE_SETTINGS));
    }

    // Use this for block access.
    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(EntityPlayer entityPlayer, IHasOwner ownedBlock) {
        Player player = new Player(entityPlayer);
        return canPlayerAccessBlock(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(Player player, IHasOwner ownedBlock) {
        if (OMConfig.GENERAL.canOPAccessOwnedBlocks && isPlayerOP(player)) {
            return true;
        }
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, ownedBlock);
        return canPlayerChangeSetting(player, ownedBlock) || (trustedPlayer != null &&
                trustedPlayer.getAccessLevel().equals(EnumAccessLevel.OPEN_GUI));
    }

    @ParametersAreNonnullByDefault
    public static void addChatMessage(ICommandSender sender, ITextComponent component) {
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }

    @ParametersAreNonnullByDefault
    public static EnumAccessLevel getPlayerAccessLevel(EntityPlayer player, IHasOwner ownedBlock) {
        if (isPlayerOwner(player, ownedBlock)) {
            return EnumAccessLevel.ADMIN;
        }
        if (isPlayerAdmin(player, ownedBlock)) {
            return EnumAccessLevel.ADMIN;
        }
        if (canPlayerChangeSetting(player, ownedBlock)) {
            return EnumAccessLevel.CHANGE_SETTINGS;
        }
        if (canPlayerAccessBlock(player, ownedBlock)) {
            return EnumAccessLevel.OPEN_GUI;
        }
        return EnumAccessLevel.NONE;
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
