package omtteam.omlib.util.player;

import omtteam.omlib.util.EnumAccessMode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@SuppressWarnings("unused")
public class TrustedPlayer {
    private String name;
    private EnumAccessMode accessMode = EnumAccessMode.NONE;
    private UUID uuid;
    private boolean hacked = false;

    @ParametersAreNonnullByDefault
    public TrustedPlayer(String name) {
        this.name = name;
    }

    @ParametersAreNonnullByDefault
    public TrustedPlayer(Player player) {
        this.name = player.getName();
        this.uuid = player.getUuid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumAccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(EnumAccessMode accessMode) {
        this.accessMode = accessMode;
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
}
