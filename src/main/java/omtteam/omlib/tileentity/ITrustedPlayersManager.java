package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.TrustedPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static omtteam.omlib.util.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.PlayerUtil.getPlayerUUID;

/**
 * Created by Keridos on 09/02/17.
 * This Class
 */
public interface ITrustedPlayersManager {

    String getOwner();

    World getWorld();

    @ParametersAreNonnullByDefault
    default boolean addTrustedPlayer(String name) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(name);
        trustedPlayer.uuid = getPlayerUUID(name);

        if (!this.getWorld().isRemote) {
            boolean foundPlayer = false;
            for (Map.Entry<UUID, String> serverName : UsernameCache.getMap().entrySet()) {
                if (name.equals(serverName.getValue())) {
                    foundPlayer = true;
                    break;
                }
            }

            if (!foundPlayer) {
                return false;
            }
        }

        if (ConfigHandler.offlineModeSupport) {
            if (trustedPlayer.getName().equals(getOwner())) {
                return false;
            }

        } else {
            if (trustedPlayer.uuid == null || trustedPlayer.uuid.toString().equals(getOwner())) {
                return false;
            }
        }

        if (trustedPlayer.uuid != null || ConfigHandler.offlineModeSupport) {
            for (TrustedPlayer player : getTrustedPlayers()) {
                if (ConfigHandler.offlineModeSupport) {
                    if (player.getName().toLowerCase().equals(name.toLowerCase()) || player.getName().equals(getOwner())) {
                        return false;
                    }
                } else {
                    if (player.getName().toLowerCase().equals(name.toLowerCase()) || trustedPlayer.uuid.toString().equals(
                            getOwner())) {
                        return false;
                    }
                }
            }
            getTrustedPlayers().add(trustedPlayer);
            return true;
        }
        return false;
    }


    default boolean removeTrustedPlayer(String name) {
        for (TrustedPlayer player : getTrustedPlayers()) {
            if (player.getName().equals(name)) {
                getTrustedPlayers().remove(player);
                return true;
            }
        }
        return false;
    }

    default TrustedPlayer getTrustedPlayer(String name) {
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            if (trustedPlayer.name.equals(name)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    default TrustedPlayer getTrustedPlayer(UUID uuid) {
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            if (trustedPlayer.uuid.equals(uuid)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    List<TrustedPlayer> getTrustedPlayers();

    @SuppressWarnings("ConstantConditions")
    default NBTTagList getTrustedPlayersAsNBT() {
        NBTTagList nbt = new NBTTagList();
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            NBTTagCompound nbtPlayer = new NBTTagCompound();
            nbtPlayer.setString("name", trustedPlayer.name);
            nbtPlayer.setBoolean("canOpenGUI", trustedPlayer.canOpenGUI);
            nbtPlayer.setBoolean("canChangeTargeting", trustedPlayer.canChangeTargeting);
            nbtPlayer.setBoolean("admin", trustedPlayer.admin);
            if (trustedPlayer.uuid != null) {
                nbtPlayer.setString("UUID", trustedPlayer.uuid.toString());
            } else if (getPlayerUUID(trustedPlayer.name) != null) {
                nbtPlayer.setString("UUID", getPlayerUUID(trustedPlayer.name).toString());
            }
            nbt.appendTag(nbtPlayer);
        }
        return nbt;
    }

    default void buildTrustedPlayersFromNBT(NBTTagList nbt) {
        getTrustedPlayers().clear();
        for (int i = 0; i < nbt.tagCount(); i++) {
            if (!nbt.getCompoundTagAt(i).getString("name").equals("")) {
                NBTTagCompound nbtPlayer = nbt.getCompoundTagAt(i);
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbtPlayer.getString("name"));
                trustedPlayer.canOpenGUI = nbtPlayer.getBoolean("canOpenGUI");
                trustedPlayer.canChangeTargeting = nbtPlayer.getBoolean("canChangeTargeting");
                trustedPlayer.admin = nbtPlayer.getBoolean("admin");
                if (nbtPlayer.hasKey("UUID")) {
                    trustedPlayer.uuid = getPlayerUIDUnstable(nbtPlayer.getString("UUID"));
                } else {
                    trustedPlayer.uuid = getPlayerUUID(trustedPlayer.name);
                }
                if (trustedPlayer.uuid != null) {
                    getTrustedPlayers().add(trustedPlayer);
                }
            } else if (nbt.getCompoundTagAt(i).getString("name").equals("")) {
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbt.getStringTagAt(i));
                Logger.getGlobal().info("found legacy trusted Player: " + nbt.getStringTagAt(i));
                trustedPlayer.uuid = getPlayerUUID(trustedPlayer.name);
                if (trustedPlayer.uuid != null) {
                    getTrustedPlayers().add(trustedPlayer);
                }
            }
        }
    }
}
