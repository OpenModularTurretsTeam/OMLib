package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.EnumAccessMode;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.TrustedPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static omtteam.omlib.util.player.PlayerUtil.*;

/**
 * Created by Keridos on 09/02/17.
 * This Class
 */
public interface ITrustedPlayersManager {
    @ParametersAreNonnullByDefault
    default boolean addTrustedPlayer(String name) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(name);
        trustedPlayer.setUuid(getPlayerUUID(name));

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
                if (trustedPlayer.getUuid() == null || isPlayerOwner(player, (TileEntityOwnedBlock) this)) {
                    DebugHandler.getInstance().sendMessageToDebugChat("You cannot add an owner!");
                    return false;
                }
            }

            if (trustedPlayer.getUuid() != null || ConfigHandler.offlineModeSupport) {
                for (TrustedPlayer existPlayer : getTrustedPlayers()) {
                    if (ConfigHandler.offlineModeSupport) {
                        if (existPlayer.getName().toLowerCase().equals(name.toLowerCase())) {
                            DebugHandler.getInstance().sendMessageToDebugChat("Already on trust list!");
                            return false;
                        }
                    } else {
                        if (existPlayer.getName().toLowerCase().equals(name.toLowerCase()) || trustedPlayer.getUuid().equals(existPlayer.getUuid())) {
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
            if (trustedPlayer.getName().equals(name)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    default TrustedPlayer getTrustedPlayer(UUID uuid) {
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            if (trustedPlayer.getUuid().equals(uuid)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    default TrustedPlayer getTrustedPlayer(Player player) {
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            if (trustedPlayer.getUuid().equals(player.getUuid()) ||
                    (ConfigHandler.offlineModeSupport && trustedPlayer.getName().equals(player.getName()))) {
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
            nbtPlayer.setString("name", trustedPlayer.getName());
            nbtPlayer.setInteger("accessLevel", trustedPlayer.getAccessMode().ordinal());
            if (trustedPlayer.getUuid() != null) {
                nbtPlayer.setString("UUID", trustedPlayer.getUuid().toString());
            } else if (getPlayerUUID(trustedPlayer.getName()) != null) {
                nbtPlayer.setString("UUID", getPlayerUUID(trustedPlayer.getName()).toString());
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
                // TODO: Remove on 1.13
                if (nbtPlayer.hasKey("canOpenGUI") && nbtPlayer.getBoolean("canOpenGUI")) {
                    trustedPlayer.setAccessMode(EnumAccessMode.OPEN_GUI);
                }
                if (nbtPlayer.hasKey("canChangeTargeting") && nbtPlayer.getBoolean("canChangeTargeting")) {
                    trustedPlayer.setAccessMode(EnumAccessMode.CHANGE_SETTINGS);
                }
                if (nbtPlayer.hasKey("admin") && nbtPlayer.getBoolean("admin")) {
                    trustedPlayer.setAccessMode(EnumAccessMode.ADMIN);
                }
                if (nbtPlayer.hasKey("accessLevel")) {
                    trustedPlayer.setAccessMode(EnumAccessMode.values()[nbtPlayer.getInteger("AccessLevel")]);
                }

                if (nbtPlayer.hasKey("UUID")) {
                    trustedPlayer.setUuid(getPlayerUIDUnstable(nbtPlayer.getString("UUID")));
                } else {
                    trustedPlayer.setUuid(getPlayerUUID(trustedPlayer.getName()));
                }
                if (trustedPlayer.getUuid() != null) {
                    getTrustedPlayers().add(trustedPlayer);
                }
            } else if (nbt.getCompoundTagAt(i).getString("name").equals("")) {
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbt.getStringTagAt(i));
                Logger.getGlobal().info("found legacy trusted Player: " + nbt.getStringTagAt(i));
                trustedPlayer.setUuid(getPlayerUUID(trustedPlayer.getName()));
                if (trustedPlayer.getUuid() != null) {
                    getTrustedPlayers().add(trustedPlayer);
                }
            }
        }
    }

    TileEntityOwnedBlock getOwnedBlock();
}
