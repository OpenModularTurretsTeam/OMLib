package omtteam.omlib.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.tileentity.TileEntityMachine;
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

@SuppressWarnings("unused")
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
    public static TrustedPlayer getTrustedPlayer(EntityPlayer player, TileEntityMachine machine) {
        if (machine.getTrustedPlayer(player.getUniqueID()) != null || (offlineModeSupport && machine.getTrustedPlayer(player.getName()) != null)) {
            return (machine.getTrustedPlayer(player.getUniqueID()) == null ? machine.getTrustedPlayer(player.getName()) : machine.getTrustedPlayer(player.getUniqueID()));
        } else {
            return null;
        }
    }

    @ParametersAreNonnullByDefault
    public static boolean isPlayerOwner(EntityPlayer player, TileEntityOwnedBlock ownedBlock) {
        return (ownedBlock.getOwner().equals(player.getUniqueID().toString()) ||
                (offlineModeSupport && ownedBlock.getOwnerName().equals(player.getName())));
    }
}
