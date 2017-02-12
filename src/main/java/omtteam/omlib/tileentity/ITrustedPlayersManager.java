package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import omtteam.omlib.util.TrustedPlayer;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static omtteam.omlib.util.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.PlayerUtil.getPlayerUUID;

/**
 * Created by Keridos on 09/02/17.
 * This Class
 */
public interface ITrustedPlayersManager {

    boolean addTrustedPlayer(String name);

    boolean removeTrustedPlayer(String name);

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
