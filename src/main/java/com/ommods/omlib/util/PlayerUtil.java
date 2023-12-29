package com.ommods.omlib.util;
import com.ommods.omlib.block.OMBlockEntity;
import com.ommods.omlib.config.OMConfig;
import com.ommods.omlib.util.permission.EnumAccessLevel;
import com.ommods.omlib.util.permission.EnumPlayerAccessType;
import com.ommods.omlib.util.permission.IHasTrustManager;
import com.ommods.omlib.util.permission.TrustedPlayer;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.UsernameCache;

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

    private PlayerUtil() {
    }
    @Nullable
    public static IHasTrustManager getTrustMachine(OMBlockEntity tile) {
        if (tile instanceof IHasTrustManager trustManager) {
            return trustManager;
        }
        return null;
    }

    @Nullable
    public static TrustedPlayer getTrustedPlayer(Player player, OMBlockEntity tile) {
        return getTrustedPlayer(new OMPlayer(player), tile);
    }

    @Nullable
    public static TrustedPlayer getTrustedPlayer(OMPlayer player, OMBlockEntity tile) {
        if (tile instanceof IHasTrustManager trustManager) {
            return trustManager.getTrustManager().getTrustedPlayer(player);
        }
        return null;
    }

    @ParametersAreNullableByDefault
    public static boolean isPlayerOP(Player player) {
        if (player != null) {
            return player.hasPermissions(4);
        }
        return false;
    }

    @ParametersAreNullableByDefault
    public static boolean isPlayerOP(OMPlayer player) {
        if (player != null) {
            Player entityPlayer = player.getPlayer();
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
    public static EnumPlayerAccessType getPlayerAccessType(Player entityPlayer, OMBlockEntity ownedBlock) {
        OMPlayer player = new OMPlayer(entityPlayer);
        return getPlayerAccessType(player, ownedBlock);
    }

    /**
     * @param player the player to check
     * @param block  the block to check against
     * @return the type of the players access to the block sorted by owner > trusted > op > none
     */
    @ParametersAreNonnullByDefault
    public static EnumPlayerAccessType getPlayerAccessType(OMPlayer player, Object block) {
        OMBlockEntity ownedBlock = null;
        if (block instanceof OMBlockEntity ownedBlockEntity) {
            ownedBlock = ownedBlockEntity;
        }
        if (ownedBlock != null) {
            OMPlayer owner = ownedBlock.getOwner();
            if (player.equals(owner) /*|| OwnerShareRegister.instance.isPlayerSharedOwner(owner, player)*/) {
                return EnumPlayerAccessType.OWNER;
            }
        } else {
            return EnumPlayerAccessType.NONE;
        }
        if (getTrustedPlayer(player, ownedBlock) != null) {
            return EnumPlayerAccessType.TRUSTED;
        } else if (Boolean.TRUE.equals(OMConfig.Permission.canOPAccessOwnedBlocks) && isPlayerOP(player)) {
            return EnumPlayerAccessType.OP;
        }
        return EnumPlayerAccessType.NONE;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(Player entityPlayer, OMBlockEntity ownedBlock) {
        OMPlayer player = new OMPlayer(entityPlayer);
        return isPlayerTrusted(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerTrusted(OMPlayer player, OMBlockEntity ownedBlock) {
        return getTrustedPlayer(player, ownedBlock) != null;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(Player checkPlayer, OMBlockEntity ownedBlock) {
        OMPlayer player = new OMPlayer(checkPlayer);
        return isPlayerOwner(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(OMPlayer player, OMBlockEntity ownedBlock) {
        return player.equals(ownedBlock.getOwner())/*
                || OwnerShareRegister.instance.isPlayerSharedOwner(ownedBlock.getOwner(), player)*/;
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(Player checkPlayer, OMBlockEntity machine) {
        OMPlayer player = new OMPlayer(checkPlayer);
        return isPlayerAdmin(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerAdmin(OMPlayer player, OMBlockEntity ownedBlock) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, ownedBlock);
        return isPlayerOwner(player, ownedBlock) || (trustedPlayer != null &&
                trustedPlayer.getAccessLevel().equals(EnumAccessLevel.ADMIN));
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(Player checkPlayer, OMBlockEntity machine) {
        OMPlayer player = new OMPlayer(checkPlayer);
        return canPlayerChangeSetting(player, machine);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerChangeSetting(OMPlayer player, OMBlockEntity ownedBlock) {
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, ownedBlock);
        return isPlayerAdmin(player, ownedBlock) || (trustedPlayer != null &&
                trustedPlayer.getAccessLevel().equals(EnumAccessLevel.CHANGE_SETTINGS));
    }

    // Use this for block access.
    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(Player entityPlayer, OMBlockEntity ownedBlock) {
        OMPlayer player = new OMPlayer(entityPlayer);
        return canPlayerAccessBlock(player, ownedBlock);
    }

    @ParametersAreNonnullByDefault
    public static boolean canPlayerAccessBlock(OMPlayer player, OMBlockEntity ownedBlock) {
        if (Boolean.TRUE.equals(OMConfig.Permission.canOPAccessOwnedBlocks) && isPlayerOP(player)) {
            return true;
        }
        TrustedPlayer trustedPlayer = getTrustedPlayer(player, ownedBlock);
        return canPlayerChangeSetting(player, ownedBlock) || (trustedPlayer != null &&
                trustedPlayer.getAccessLevel().equals(EnumAccessLevel.OPEN_GUI));
    }

    @ParametersAreNonnullByDefault
    public static EnumAccessLevel getPlayerAccessLevel(OMPlayer player, OMBlockEntity ownedBlock) {
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
    public static OMPlayer getPlayerFromUsernameCache(String name) {
        for (Map.Entry<UUID, String> serverName : UsernameCache.getMap().entrySet()) {
            if (name.equals(serverName.getValue())) {
                return new OMPlayer(serverName.getKey(), serverName.getValue());
            }
        }
        return null;
    }
}