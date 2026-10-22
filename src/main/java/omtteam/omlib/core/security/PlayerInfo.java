package omtteam.omlib.core.security;

import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot of an online player that wants to do something with a block. Created by the Minecraft layer.
 */
public final class PlayerInfo {
    private final UUID uuid;
    private final String name;
    private final boolean operator;

    /** @param operator whether the player has operator permission level 4 ({@code PlayerUtil.isPlayerOP}) */
    public PlayerInfo(UUID uuid, String name, boolean operator) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNull(name, "name");
        this.operator = operator;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public boolean operator() {
        return operator;
    }

    public PlayerIdentity identity() {
        return new PlayerIdentity(uuid, name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerInfo)) {
            return false;
        }
        PlayerInfo other = (PlayerInfo) o;
        return uuid.equals(other.uuid) && name.equals(other.name) && operator == other.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, operator);
    }

    @Override
    public String toString() {
        return name + "/" + uuid + (operator ? " (op)" : "");
    }
}
