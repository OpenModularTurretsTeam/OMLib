package com.ommods.omlib.util.permission;

import com.ommods.omlib.config.OMConfig;
import com.ommods.omlib.util.OMPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class TrustedPlayer {
    private String name;
    private EnumAccessLevel accessLevel = EnumAccessLevel.NONE;
    private UUID uuid;
    private boolean hacked = false;

    @ParametersAreNonnullByDefault
    public TrustedPlayer(String name) {
        this.name = name;
    }

    @ParametersAreNonnullByDefault
    public TrustedPlayer(OMPlayer player) {
        this.name = player.getName();
        this.uuid = player.getUuid();
    }

    @ParametersAreNonnullByDefault
    public TrustedPlayer(OMPlayer player, EnumAccessLevel accessLevel) {
        this.name = player.getName();
        this.uuid = player.getUuid();
        this.accessLevel = accessLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumAccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(EnumAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isHacked() {
        return hacked;
    }

    public void setHacked(boolean hacked) {
        this.hacked = hacked;
    }

    public OMPlayer getOMPlayer() {
        return new OMPlayer(this.uuid, this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrustedPlayer that = (TrustedPlayer) o;
        return hacked == that.hacked &&
                name.equals(that.name) &&
                accessLevel == that.accessLevel &&
                (OMConfig.Permission.offlineModeSupport || Objects.equals(uuid, that.uuid));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, accessLevel, uuid, hacked);
    }

    public Map<String, String> asMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", this.name);
        map.put("accessLevel", this.accessLevel.ordinal() + ": " + this.accessLevel.getName());
        return map;
    }
}
