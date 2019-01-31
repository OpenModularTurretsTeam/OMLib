package omtteam.omlib.util.player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.api.permission.ITrustedPlayersManager;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.handler.OwnerShareHandler;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.Map;
import java.util.UUID;

import static omtteam.omlib.handler.ConfigHandler.offlineModeSupport;

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
    public static TrustedPlayer getTrustedPlayer(EntityPlayer entityPlayer, ITrustedPlayersManager machine) {
        Player player = new Player(entityPlayer);
        return getTrustedPlayer(player, machine);
    }

    @Nullable
    @ParametersAreNonnullByDefault
    public static TrustedPlayer getTrustedPlayer(Player player, ITrustedPlayersManager machine) {
        if (machine.getTrustedPlayer(player.getUuid()) != null || (offlineModeSupport && machine.getTrustedPlayer(player.getName()) != null)) {
            return (machine.getTrustedPlayer(player.getUuid()) == null ? machine.getTrustedPlayer(player.getName()) : machine.getTrustedPlayer(player.getUuid()));
        } else {
            return null;
        }
    }

    // Use this for determining what the access type for the player would be.
    // (needed when OP access is active and OPs need to be treated the same in some regions)
    @ParametersAreNonnullByDefault
    public static EnumPlayerType getPlayerType(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(entityPlayer);
        return getPlayerType(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static EnumPlayerType getPlayerType(Player player, TileEntityOwnedBlock ownedBlock) {
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        if (player.equals(owner) || OwnerShareHandler.getInstance().isPlayerSharedOwner(owner, player)) {
            return EnumPlayerType.OWNER;
        } else if (ownedBlock instanceof ITrustedPlayersManager && isPlayerTrusted(player, (ITrustedPlayersManager) ownedBlock)) {
            return EnumPlayerType.TRUSTED;
        } else if (ConfigHandler.canOPAccessOwnedBlocks && isPlayerOP(player)) {
            return EnumPlayerType.OP;
        }
        return EnumPlayerType.NONE;
    }

    // Use this for block access.
    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(EntityPlayer entityPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(entityPlayer);
        return canPlayerAccessBlock(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(Player player, TileEntityOwnedBlock ownedBlock) {
        ITrustedPlayersManager machine = (ownedBlock instanceof ITrustedPlayersManager ? (ITrustedPlayersManager) ownedBlock : null);
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        TrustedPlayer trustedPlayer = machine != null ? machine.getTrustedPlayer(player) : null;
        if (ConfigHandler.canOPAccessOwnedBlocks && isPlayerOP(player)) {
            return true;
        } else if (isPlayerOwner(player, ownedBlock)) {
            return true;
        }
        return (machine != null && trustedPlayer != null && (isPlayerAdmin(player, machine) ||
                canPlayerChangeSetting(player, machine) ||
                trustedPlayer.getAccessMode().equals(EnumAccessMode.OPEN_GUI)));
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(EntityPlayer entityPlayer, ITrustedPlayersManager trustedPlayersManager) {
        Player player = new Player(entityPlayer);
        return isPlayerTrusted(player, trustedPlayersManager);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(Player player, ITrustedPlayersManager trustedPlayersManager) {
        return (offlineModeSupport ? trustedPlayersManager.getTrustedPlayer(player.getName()) != null :
                trustedPlayersManager.getTrustedPlayer(player.getUuid()) != null);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(EntityPlayer checkPlayer, TileEntityOwnedBlock ownedBlock) {
        Player player = new Player(checkPlayer);
        return isPlayerOwner(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(Player player, TileEntityOwnedBlock ownedBlock) {
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        return player.equals(owner) || OwnerShareHandler.getInstance().isPlayerSharedOwner(owner, player);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(EntityPlayer checkPlayer, ITrustedPlayersManager machine) {
        Player player = new Player(checkPlayer);
        return isPlayerAdmin(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(Player player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.ADMIN));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(EntityPlayer checkPlayer, ITrustedPlayersManager machine) {
        Player player = new Player(checkPlayer);
        return canPlayerChangeSetting(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(Player player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || isPlayerAdmin(player, machine)
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.CHANGE_SETTINGS));
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
    public static EnumAccessMode getPlayerAccess(EntityPlayer player, TileEntityOwnedBlock ownedBlock) {
        if (ownedBlock instanceof ITrustedPlayersManager) {
            ITrustedPlayersManager trustedPlayersManager = (ITrustedPlayersManager) ownedBlock;
            TrustedPlayer trustedPlayer = trustedPlayersManager.getTrustedPlayer(new Player(player));
            if (trustedPlayer != null) {
                if (isPlayerAdmin(player, trustedPlayersManager)) {
                    return EnumAccessMode.ADMIN;
                }
                if (canPlayerChangeSetting(player, trustedPlayersManager)) {
                    return EnumAccessMode.CHANGE_SETTINGS;
                }
                if (canPlayerAccessBlock(player, ownedBlock)) {
                    return EnumAccessMode.OPEN_GUI;
                }
            }
        }
        if (isPlayerOwner(player, ownedBlock)) {
            return EnumAccessMode.ADMIN;
        }
        return EnumAccessMode.NONE;
    }
}
