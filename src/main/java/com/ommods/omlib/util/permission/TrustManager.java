package com.ommods.omlib.util.permission;

import com.ommods.omlib.config.OMConfig;
import com.ommods.omlib.util.OMPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static com.ommods.omlib.util.PlayerUtil.getPlayerUIDUnstable;
import static com.ommods.omlib.util.PlayerUtil.getPlayerUUID;

public class TrustManager {

    protected List<TrustedPlayer> trustedPlayers = new ArrayList<>();
    OMPlayer owner;

    public boolean addTrustedPlayer(TrustedPlayer player) {
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.equals(player)) {
                return false;
            }
        }
        if (!OMConfig.Permission.offlineModeSupport && player.getUuid() == null) {
            return false;
        }
        this.trustedPlayers.add(player);
        return true;
    }

    public boolean removeTrustedPlayer(String name) {
        for (TrustedPlayer player : this.trustedPlayers) {
            if (player.getName().equals(name)) {
                this.trustedPlayers.remove(player);
                //DebugHandler.getInstance().sendMessageToDebugChat("Sucessfully removed " + name + ".");
                return true;
            }
        }
        //DebugHandler.getInstance().sendMessageToDebugChat("Could not remove " + name + ".");
        return false;
    }

    public TrustedPlayer getTrustedPlayer(String name) {
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.getName().equals(name)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    public TrustedPlayer getTrustedPlayer(UUID uuid) {
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.getUuid().equals(uuid)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    public TrustedPlayer getTrustedPlayer(OMPlayer player) {
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            if ((OMConfig.Permission.offlineModeSupport && trustedPlayer.getName().equals(player.getName())) ||
                    (trustedPlayer.getUuid() != null && trustedPlayer.getUuid().equals(player.getUuid()))) {
                return trustedPlayer;
            }
        }
        return null;
    }

    public List<Map<String, String>> getTrustedPlayersAsListMap() {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (TrustedPlayer tp : this.trustedPlayers) {
            list.add(tp.asMap());
        }
        return list;
    }

    @SuppressWarnings("ConstantConditions")
    public CompoundTag writeToNBT(CompoundTag nbt) {
        ListTag nbtList = new ListTag();
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            CompoundTag nbtPlayer = new CompoundTag();
            nbtPlayer.putString("name", trustedPlayer.getName());
            nbtPlayer.putInt("accessLevel", trustedPlayer.getAccessLevel().ordinal());
            if (trustedPlayer.getUuid() != null) {
                nbtPlayer.putString("UUID", trustedPlayer.getUuid().toString());
            } else if (getPlayerUUID(trustedPlayer.getName()) != null) {
                nbtPlayer.putString("UUID", getPlayerUUID(trustedPlayer.getName()).toString());
            }
            nbtList.add(nbtPlayer);
        }
        nbt.put("trustedPlayers", nbtList);
        return nbt;
    }

    public void readFromNBT(CompoundTag tagCompound) {
        this.trustedPlayers.clear();
        ListTag list = tagCompound.getList("trustedPlayers", tagCompound.getId());
        for (int i = 0; i < list.size(); i++) {
            if (!list.getCompound(i).getString("name").isEmpty()) {
                CompoundTag nbtPlayer = list.getCompound(i);
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbtPlayer.getString("name"));
                if (nbtPlayer.contains("accessLevel")) {
                    trustedPlayer.setAccessLevel(EnumAccessLevel.values()[nbtPlayer.getInt("accessLevel")]);
                } else if (nbtPlayer.contains("admin") && nbtPlayer.getBoolean("admin")) {
                    trustedPlayer.setAccessLevel(EnumAccessLevel.ADMIN);
                } else if (nbtPlayer.contains("canChangeTargeting") && nbtPlayer.getBoolean("canChangeTargeting")) {
                    trustedPlayer.setAccessLevel(EnumAccessLevel.CHANGE_SETTINGS);
                } else if (nbtPlayer.contains("canOpenGUI") && nbtPlayer.getBoolean("canOpenGUI")) {
                    trustedPlayer.setAccessLevel(EnumAccessLevel.OPEN_GUI);
                }
                if (nbtPlayer.contains("UUID")) {
                    trustedPlayer.setUuid(getPlayerUIDUnstable(nbtPlayer.getString("UUID")));
                } else {
                    trustedPlayer.setUuid(getPlayerUUID(trustedPlayer.getName()));
                }
                if (trustedPlayer.getUuid() != null) {
                    this.trustedPlayers.add(trustedPlayer);
                }
            } else if (list.getCompound(i).getString("name").equals("")) {
                TrustedPlayer trustedPlayer = new TrustedPlayer(list.getString(i));
                Logger.getGlobal().info("found legacy trusted Player: " + list.getString(i));
                trustedPlayer.setUuid(getPlayerUUID(trustedPlayer.getName()));
                if (trustedPlayer.getUuid() != null) {
                    this.trustedPlayers.add(trustedPlayer);
                }
            }
        }
    }

    public void readFromByteBuf(FriendlyByteBuf buf) {
        ArrayList<TrustedPlayer> sharePlayerList = new ArrayList<>();
        int lengthOfPlayerList = buf.readInt();
        if (lengthOfPlayerList > 0) {
            for (int j = 0; j < lengthOfPlayerList; j++) {
                TrustedPlayer player = new TrustedPlayer(OMPlayer.readFromByteBuf(buf));
                player.setAccessLevel(EnumAccessLevel.values()[buf.readInt()]);
                sharePlayerList.add(player);
            }
        }
        this.trustedPlayers = sharePlayerList;
    }

    public void writeToByteBuf(FriendlyByteBuf buf) {
        buf.writeInt(this.trustedPlayers.size());
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            OMPlayer.writeToByteBuf(trustedPlayer.getOMPlayer(), buf);
            buf.writeInt(trustedPlayer.getAccessLevel().ordinal());
        }
    }

    @SuppressWarnings("unused")
    public boolean changePermission(String player, EnumAccessLevel level) {
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.getName().equalsIgnoreCase(player)) {
                trustedPlayer.setAccessLevel(level);
                return true;
            }
        }
        return false;
    }

    public boolean changePermission(String player, int change) {
        for (TrustedPlayer trustedPlayer : this.trustedPlayers) {
            if (trustedPlayer.getName().equalsIgnoreCase(player)) {
                if (trustedPlayer.getAccessLevel().ordinal() == 0 && change == -1
                        || trustedPlayer.getAccessLevel().ordinal() == 3 && change == 1) {
                    return false;
                } else {
                    trustedPlayer.setAccessLevel(EnumAccessLevel.values()[trustedPlayer.getAccessLevel().ordinal() + change]);
                }
                return true;
            }
        }
        return false;
    }
}