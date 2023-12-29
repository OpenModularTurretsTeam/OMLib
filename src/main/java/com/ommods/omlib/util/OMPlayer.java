package com.ommods.omlib.util;

import com.ommods.omlib.config.OMConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Objects;
import java.util.UUID;

public class OMPlayer extends OMTarget implements INBTSerializable<CompoundTag> {
    private UUID uuid;
    private String name;
    private String team;
    private Player player;


    public OMPlayer() {

    }

    public OMPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public OMPlayer(UUID uuid, String name, String team) {
        this.uuid = uuid;
        this.name = name;
        this.team = team;
    }
    public OMPlayer(Player player, boolean save) {
        super(player);
        this.uuid = player.getUUID();
        this.name = player.getName().getString();
        // Set targettype when creating from Player Entity
        this.targetType = TargetType.PLAYER;
        if (player.getTeam() != null) {
            this.team = player.getTeam().getName();
        }
        if (save) this.player = player;
    }
    public OMPlayer(Player player) {
        super(player);
        this.uuid = player.getUUID();
        this.name = player.getName().getString();
        // Set targettype when creating from Player Entity
        this.targetType = TargetType.PLAYER;
        if (player.getTeam() != null) {
            this.team = player.getTeam().getName();
        }
    }

    public OMPlayer(CompoundTag tag) {
        this.deserializeNBT(tag);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getTeam() {
        return team;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public CompoundTag serializeNBT() {
        // Used for storing trusted players and owners
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("uuid", uuid.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.name = nbt.getString("name");
        this.uuid = UUID.fromString(nbt.getString("uuid"));
    }

    public static void writeToByteBuf(OMPlayer player, FriendlyByteBuf buf) {
        buf.writeUtf(player.name);
        buf.writeUtf(player.getUuid().toString());
        buf.writeUtf(player.getTeam());
    }

    public static OMPlayer readFromByteBuf(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        UUID uuid = UUID.fromString(buf.readUtf());
        String teamUUID = buf.readUtf();
        return new OMPlayer(uuid, name, teamUUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OMPlayer omPlayer = (OMPlayer) o;
        return (OMConfig.Permission.offlineModeSupport || Objects.equals(uuid, omPlayer.uuid)) && Objects.equals(name, omPlayer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }
}
