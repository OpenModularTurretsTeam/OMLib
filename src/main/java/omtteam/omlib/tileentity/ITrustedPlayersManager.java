package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.Player;
import omtteam.omlib.util.TrustedPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static omtteam.omlib.util.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.PlayerUtil.getPlayerUUID;
import static omtteam.omlib.util.PlayerUtil.isPlayerOwner;

/**
 * Created by Keridos on 09/02/17.
 * This Class
 */
public interface ITrustedPlayersManager {
    @ParametersAreNonnullByDefault
    default boolean addTrustedPlayer(String name) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(name);
        trustedPlayer.uuid = getPlayerUUID(name);

        if (!((TileEntityOwnedBlock) this).getWorld().isRemote) {
            Player player = null;
            boolean foundPlayer = false;
            for (Map.Entry<UUID, String> serverName : UsernameCache.getMap().entrySet()) {
                if (name.equals(serverName.getValue())) {
                    player = new Player(serverName.getKey(), serverName.getValue());
                    foundPlayer = true;
                    break;
                }
            }
            if (!foundPlayer) {
                player = new Player(null, name);
            }


            if (!foundPlayer && !ConfigHandler.offlineModeSupport) {
                DebugHandler.getInstance().sendMessageToDebugChat("Did not find player named " + name + "in the username cache.");
                return false;
            }


            if (ConfigHandler.offlineModeSupport) {
                if (isPlayerOwner(player, (TileEntityOwnedBlock) this)) {
                    DebugHandler.getInstance().sendMessageToDebugChat("You cannot add an owner!");
                    return false;
                }

            } else {
                if (trustedPlayer.uuid == null || isPlayerOwner(player, (TileEntityOwnedBlock) this)) {
                    DebugHandler.getInstance().sendMessageToDebugChat("You cannot add an owner!");
                    return false;
                }
            }

            if (trustedPlayer.uuid != null || ConfigHandler.offlineModeSupport) {
                for (TrustedPlayer existPlayer : getTrustedPlayers()) {
                    if (ConfigHandler.offlineModeSupport) {
                        if (existPlayer.getName().toLowerCase().equals(name.toLowerCase())) {
                            DebugHandler.getInstance().sendMessageToDebugChat("Already on trust list!");
                            return false;
                        }
                    } else {
                        if (existPlayer.getName().toLowerCase().equals(name.toLowerCase()) || trustedPlayer.uuid.equals(player.getUuid())) {
                            return false;
                        }
                    }
                }
                DebugHandler.getInstance().sendMessageToDebugChat("Sucessfully added " + name + ".");
                getTrustedPlayers().add(trustedPlayer);
                return true;
            }
        }
        return false;
    }

    default boolean removeTrustedPlayer(String name) {
        for (TrustedPlayer player : getTrustedPlayers()) {
            if (player.getName().equals(name)) {
                getTrustedPlayers().remove(player);
                DebugHandler.getInstance().sendMessageToDebugChat("Sucessfully removed " + name + ".");
                return true;
            }
        }
        DebugHandler.getInstance().sendMessageToDebugChat("Could not remove " + name + ".");
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
