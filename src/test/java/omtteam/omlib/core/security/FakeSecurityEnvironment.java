package omtteam.omlib.core.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Test double for {@link SecurityEnvironment}: teams by player name, shared owners by UUID, switchable config flags. */
class FakeSecurityEnvironment implements SecurityEnvironment {
    final Map<String, String> teams = new HashMap<>();
    boolean operatorsCanAccess;
    boolean damageTrusted;
    boolean offlineMode;
    /** UUIDs of the players every owner shares ownership with. */
    final Set<UUID> sharedOwners = new HashSet<>();

    @Override
    public Optional<String> getTeam(UUID uuid, String name) {
        return Optional.ofNullable(teams.get(name));
    }

    @Override
    public boolean operatorsCanAccessOwnedBlocks() {
        return operatorsCanAccess;
    }

    @Override
    public boolean turretsDamageTrustedPlayers() {
        return damageTrusted;
    }

    @Override
    public boolean isSharedOwner(PlayerIdentity owner, PlayerIdentity player) {
        return sharedOwners.contains(player.uuid());
    }

    @Override
    public boolean offlineModeSupport() {
        return offlineMode;
    }
}
