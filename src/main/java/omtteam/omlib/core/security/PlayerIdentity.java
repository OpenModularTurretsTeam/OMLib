package omtteam.omlib.core.security;

import java.util.Objects;
import java.util.UUID;

/**
 * A player as stored by a block (owner or trusted player). The UUID is the identity; the name is the last known name
 * and only used for display (and for matching in offline mode, see {@link SecurityEnvironment#offlineModeSupport()}).
 */
public final class PlayerIdentity {
    private final UUID uuid;
    private final String name;

    public PlayerIdentity(UUID uuid, String name) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNull(name, "name");
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public boolean is(UUID other) {
        return uuid.equals(other);
    }

    /** Same identity, possibly with a different (newer) name. */
    public PlayerIdentity withName(String newName) {
        return new PlayerIdentity(uuid, newName);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerIdentity)) {
            return false;
        }
        PlayerIdentity other = (PlayerIdentity) o;
        return uuid.equals(other.uuid) && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return 31 * uuid.hashCode() + name.hashCode();
    }

    @Override
    public String toString() {
        return name + "/" + uuid;
    }
}
