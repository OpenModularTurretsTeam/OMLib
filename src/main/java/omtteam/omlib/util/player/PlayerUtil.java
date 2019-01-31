package omtteam.omlib.util.player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.api.permission.GlobalTrustRegister;
import omtteam.omlib.api.permission.IHasTrustManager;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.api.permission.TrustedPlayersManagerTile;
import omtteam.omlib.handler.ConfigHandler;
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
    @ParametersAreNullableByDefault
    public static boolean isPlayerOP(EntityPlayer player) {
        if (player != null) {
            return player.getServer() != null &&
                    player.getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()).getPermissionLevel() == 4;
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

    @Nullable
    @ParametersAreNonnullByDefault
    public static TrustedPlayer getTrustedPlayer(EntityPlayer entityPlayer, IHasTrustManager machine) {
        Player player = new Player(entityPlayer);
        return getTrustedPlayer(player, machine);
    }

    @Nullable
    @ParametersAreNonnullByDefault
    public static TrustedPlayer getTrustedPlayer(Player player, IHasTrustManager machine) {
        return machine.getTrustManager().getTrustedPlayer(player);
    }

    // Use this for determining what the access type for the player would be.
    // (needed when OP access is active and OPs need to be treated the same in some regions)
    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerType(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(entityPlayer);
        return getPlayerType(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerType(Player player, TileEntityOwnedBlock ownedBlock) {
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        if (player.equals(owner) || OwnerShareRegister.instance.isPlayerSharedOwner(owner, player)) {
            return EnumPlayerAccessType.OWNER;
        } else if (ownedBlock instanceof IHasTrustManager && isPlayerTrusted(player, ownedBlock)) {
            return EnumPlayerAccessType.TRUSTED;
        } else if (ConfigHandler.canOPAccessOwnedBlocks && isPlayerOP(player)) {
            return EnumPlayerAccessType.OP;
        }
        return EnumPlayerAccessType.NONE;
    }

    // Use this for block access.
    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(entityPlayer);
        return canPlayerAccessBlock(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(Player player, TileEntityOwnedBlock ownedBlock) {
        IHasTrustManager machine = (ownedBlock instanceof IHasTrustManager ? (IHasTrustManager) ownedBlock : null);
        TrustedPlayer trustedPlayer = machine != null ? machine.getTrustManager().getTrustedPlayer(player) : null;
        if (ConfigHandler.canOPAccessOwnedBlocks && isPlayerOP(player)) {
            return true;
        } else if (isPlayerOwner(player, ownedBlock)) {
            return true;
        }
        return (machine != null && trustedPlayer != null && (
                canPlayerChangeSetting(player, ownedBlock) ||
                        trustedPlayer.getAccessMode().equals(EnumAccessLevel.OPEN_GUI)));
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(entityPlayer);
        return isPlayerTrusted(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(Player player, TileEntityOwnedBlock ownedBlock) {
        boolean found = false;
        if (ownedBlock instanceof IHasTrustManager) {
            TrustedPlayersManagerTile trustedPlayersManager = ((IHasTrustManager) ownedBlock).getTrustManager();
            found = trustedPlayersManager.getTrustedPlayer(player) != null;
            if (!found) {
                found = GlobalTrustRegister.instance.getTrustedPlayer(ownedBlock.getOwnerAsPlayer(), player) != null;
            }
        }
        return found;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(EntityPlayer checkPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(checkPlayer);
        return isPlayerOwner(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(Player player, TileEntityOwnedBlock ownedBlock) {
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        return player.equals(owner) || OwnerShareRegister.instance.isPlayerSharedOwner(owner, player);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(EntityPlayer checkPlayer, TileEntityOwnedBlock machine) {
        Player player = new Player(checkPlayer);
        return isPlayerAdmin(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(Player player, TileEntityOwnedBlock ownedBlock) {
        IHasTrustManager machine = (ownedBlock instanceof IHasTrustManager ? (IHasTrustManager) ownedBlock : null);
        TrustedPlayer trustedPlayer = machine != null ? machine.getTrustManager().getTrustedPlayer(player) : null;
        if (isPlayerOwner(player, ownedBlock)) {
            return true;
        }
        return (machine != null && trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessLevel.ADMIN));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(EntityPlayer checkPlayer, TileEntityOwnedBlock machine) {
        Player player = new Player(checkPlayer);
        return canPlayerChangeSetting(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(Player player, TileEntityOwnedBlock ownedBlock) {
        IHasTrustManager machine = (ownedBlock instanceof IHasTrustManager ? (IHasTrustManager) ownedBlock : null);
        TrustedPlayer trustedPlayer = machine != null ? machine.getTrustManager().getTrustedPlayer(player) : null;
        return (machine != null && trustedPlayer != null && (isPlayerAdmin(player, ownedBlock) ||
                trustedPlayer.getAccessMode().equals(EnumAccessLevel.CHANGE_SETTINGS)));
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
    public static EnumAccessLevel getPlayerAccess(EntityPlayer player, TileEntityOwnedBlock ownedBlock) {
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
