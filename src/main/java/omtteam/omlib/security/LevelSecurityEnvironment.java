package omtteam.omlib.security;

import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import omtteam.omlib.core.security.SecurityEnvironment;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * {@link SecurityEnvironment} backed by a level's scoreboard and the mod's {@link OwnershipSettings}.
 */
public class LevelSecurityEnvironment implements SecurityEnvironment {
    private final Supplier<Level> level;
    private final OwnershipSettings settings;

    /** @param level supplies the current level; may return {@code null} while the block entity is loading */
    public LevelSecurityEnvironment(Supplier<Level> level, OwnershipSettings settings) {
        this.level = level;
        this.settings = settings;
    }

    @Override
    public Optional<String> getTeam(UUID uuid, String name) {
        Level current = level.get();
        if (current == null) return Optional.empty();
        PlayerTeam team = current.getScoreboard().getPlayersTeam(name);
        return team == null ? Optional.empty() : Optional.of(team.getName());
    }

    @Override
    public boolean operatorsCanAccessOwnedBlocks() {
        return settings.operatorsCanAccessOwnedBlocks();
    }

    @Override
    public boolean damageTrustedPlayers() {
        return settings.damageTrustedPlayers();
    }
}
