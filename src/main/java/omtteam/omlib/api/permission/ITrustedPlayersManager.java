package omtteam.omlib.api.permission;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static omtteam.omlib.util.player.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

public interface ITrustedPlayersManager {

    @ParametersAreNonnullByDefault
    boolean addTrustedPlayer(String name);

    default boolean addTrustedPlayer(TrustedPlayer player) {
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            if (trustedPlayer.equals(player)) {
                return false;
            }
        }
        if (!ConfigHandler.offlineModeSupport && player.getUuid() == null) {
            return false;
        }
        getTrustedPlayers().add(player);
        return true;
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

    void setTrustedPlayers(List<TrustedPlayer> trustedPlayers);

    @SuppressWarnings("ConstantConditions")
    default NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList nbtList = new NBTTagList();
        nbt.setBoolean("useGlobal", useGlobal());

        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            NBTTagCompound nbtPlayer = new NBTTagCompound();
            nbtPlayer.setString("name", trustedPlayer.getName());
            nbtPlayer.setInteger("accessLevel", trustedPlayer.getAccessLevel().ordinal());
            if (trustedPlayer.getUuid() != null) {
                nbtPlayer.setString("UUID", trustedPlayer.getUuid().toString());
            } else if (getPlayerUUID(trustedPlayer.getName()) != null) {
                nbtPlayer.setString("UUID", getPlayerUUID(trustedPlayer.getName()).toString());
            }
            nbtList.appendTag(nbtPlayer);
        }
        nbt.setTag("trustedPlayers", nbtList);
        return nbt;
    }

    default void readFromNBT(NBTTagCompound tagCompound) {
        getTrustedPlayers().clear();
        setUseGlobal(tagCompound.getBoolean("useGlobal"));
        NBTTagList list = tagCompound.getTagList("trustedPlayers", tagCompound.getId());
        for (int i = 0; i < list.tagCount(); i++) {
            if (!list.getCompoundTagAt(i).getString("name").equals("")) {
                NBTTagCompound nbtPlayer = list.getCompoundTagAt(i);
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbtPlayer.getString("name"));
                if (nbtPlayer.hasKey("accessLevel")) {
                    trustedPlayer.setAccessLevel(EnumAccessLevel.values()[nbtPlayer.getInteger("AccessLevel")]);
                }
                if (nbtPlayer.hasKey("UUID")) {
                    trustedPlayer.setUuid(getPlayerUIDUnstable(nbtPlayer.getString("UUID")));
                } else {
                    trustedPlayer.setUuid(getPlayerUUID(trustedPlayer.getName()));
                }
                if (trustedPlayer.getUuid() != null) {
                    getTrustedPlayers().add(trustedPlayer);
                }
            } else if (list.getCompoundTagAt(i).getString("name").equals("")) {
                TrustedPlayer trustedPlayer = new TrustedPlayer(list.getStringTagAt(i));
                Logger.getGlobal().info("found legacy trusted Player: " + list.getStringTagAt(i));
                trustedPlayer.setUuid(getPlayerUUID(trustedPlayer.getName()));
                if (trustedPlayer.getUuid() != null) {
                    getTrustedPlayers().add(trustedPlayer);
                }
            }
        }
    }

    default void readFromByteBuf(ByteBuf buf) {
        ArrayList<TrustedPlayer> sharePlayerList = new ArrayList<>();
        int lengthOfPlayerList = buf.readInt();
        if (lengthOfPlayerList > 0) {
            for (int j = 0; j < lengthOfPlayerList; j++) {
                TrustedPlayer player = new TrustedPlayer(Player.readFromByteBuf(buf));
                player.setAccessLevel(EnumAccessLevel.values()[buf.readInt()]);
                sharePlayerList.add(player);
            }
        }
        this.setTrustedPlayers(sharePlayerList);
    }

    default void writeToByteBuf(ByteBuf buf) {
        buf.writeInt(getTrustedPlayers().size());
        for (TrustedPlayer trustedPlayer : getTrustedPlayers()) {
            Player.writeToByteBuf(trustedPlayer.getPlayer(), buf);
            buf.writeInt(trustedPlayer.getAccessLevel().ordinal());
        }
    }

    boolean useGlobal();

    void setUseGlobal(boolean useGlobal);
}
