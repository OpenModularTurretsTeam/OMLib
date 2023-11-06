package com.ommods.omlib.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class OMPlayer extends OMTarget implements INBTSerializable<CompoundTag> {
    private UUID uuid;
    private String name;
    private String team;
    private float HP;
    private int armor;
    private Player player;


    public OMPlayer() {

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
        if (player.getTeam() != null) {
            this.team = player.getTeam().getName();
        }
        if (save) this.player = player;
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

    public float getHP() {
        if (player != null) return player.getHealth();
        return HP;
    }

    public int getArmor() {
        if (player != null) return player.getArmorValue();
        return armor;
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
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.name = nbt.getString("name");
        this.uuid = UUID.fromString(nbt.getString("uuid"));
    }
}
