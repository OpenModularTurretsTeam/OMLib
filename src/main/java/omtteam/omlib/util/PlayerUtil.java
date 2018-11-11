package omtteam.omlib.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.handler.OwnerShareHandler;
import omtteam.omlib.tileentity.ITrustedPlayersManager;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nonnull;
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

@SuppressWarnings("unused")
public class PlayerUtil {

    public static boolean isPlayerOP(EntityPlayer player) {
        return player.getServer() != null &&
                player.getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()) != null &&
                player.getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()).getPermissionLevel() == 4;
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
    public static TrustedPlayer getTrustedPlayer(EntityPlayer player, ITrustedPlayersManager machine) {
        if (machine.getTrustedPlayer(player.getUniqueID()) != null || (offlineModeSupport && machine.getTrustedPlayer(player.getName()) != null)) {
            return (machine.getTrustedPlayer(player.getUniqueID()) == null ? machine.getTrustedPlayer(player.getName()) : machine.getTrustedPlayer(player.getUniqueID()));
        } else {
            return null;
        }
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

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(EntityPlayer entityPlayer, ITrustedPlayersManager trustedPlayersManager) {
        Player player = new Player(entityPlayer.getUniqueID(), entityPlayer.getName());
        return isPlayerTrusted(player, trustedPlayersManager);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(Player player, ITrustedPlayersManager trustedPlayersManager) {
        return (offlineModeSupport ? trustedPlayersManager.getTrustedPlayer(player.getName()) != null :
                trustedPlayersManager.getTrustedPlayer(player.getUuid()) != null);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(EntityPlayer checkPlayer, TileEntityOwnedBlock ownedBlock) {
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        Player player = new Player(checkPlayer.getGameProfile().getId(), checkPlayer.getName());
        boolean allowed;
        allowed = (player.equals(owner));
        if (!allowed && (ConfigHandler.canOPAccessOwnedBlocks && isPlayerOP(checkPlayer))) {
            allowed = true;
        } else if (!allowed && OwnerShareHandler.getInstance().isPlayerSharedOwner(owner, player)) {
            allowed = true;
        }

        return allowed;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(Player player, TileEntityOwnedBlock ownedBlock) {
        Player owner = new Player(getPlayerUIDUnstable(ownedBlock.getOwner()), ownedBlock.getOwnerName());
        boolean allowed;
        allowed = (player.equals(owner));
        if (!allowed && OwnerShareHandler.getInstance().isPlayerSharedOwner(owner, player)) {
            allowed = true;
        }

        return allowed; // TODO: create enum for result, adapt all other code.
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(EntityPlayer player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.ADMIN));
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(Player player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.ADMIN));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerOpenGUI(EntityPlayer player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || isPlayerAdmin(player, machine) || canPlayerChangeSetting(player, machine)
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.OPEN_GUI));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerOpenGUI(Player player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || isPlayerAdmin(player, machine) || canPlayerChangeSetting(player, machine)
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.OPEN_GUI));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(EntityPlayer player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || isPlayerAdmin(player, machine)
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.CHANGE_SETTINGS));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(Player player, ITrustedPlayersManager machine) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, machine);
        return isPlayerOwner(player, machine.getOwnedBlock())
                || isPlayerAdmin(player, machine)
                || (trustedPlayer != null && trustedPlayer.getAccessMode().equals(EnumAccessMode.CHANGE_SETTINGS));
    }

    public static void addChatMessage(@Nonnull ICommandSender sender, @Nonnull ITextComponent component) {
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }
}
