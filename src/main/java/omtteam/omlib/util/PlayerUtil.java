package omtteam.omlib.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.tileentity.TileEntityMachine;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

import static omtteam.omlib.handler.ConfigHandler.offlineModeSupport;

/**
 * Created by Keridos on 20/07/16.
 * A lib for all player based functions.
 */

public class PlayerUtil {
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

    public static String getPlayerNameFromUUID(String possibleUUID) {
        if (possibleUUID == null || possibleUUID.isEmpty()) {
            return null;
        }
        return UsernameCache.getLastKnownUsername(UUID.fromString(possibleUUID));
    }

    public static TrustedPlayer getTrustedPlayer(EntityPlayer player, TileEntityMachine machine) {
        if (machine.getTrustedPlayer(player.getUniqueID()) != null || (offlineModeSupport && machine.getTrustedPlayer(player.getName()) != null)) {
            return (machine.getTrustedPlayer(player.getUniqueID()) == null ? machine.getTrustedPlayer(player.getName()) : machine.getTrustedPlayer(player.getUniqueID()));
        } else {
            return null;
        }
    }

    public static boolean isPlayerOwner(EntityPlayer player, TileEntityOwnedBlock ownedBlock) {
        return (ownedBlock.getOwner().equals(player.getUniqueID().toString()) ||
                (offlineModeSupport && ownedBlock.getOwnerName().equals(player.getName())));
    }
}
