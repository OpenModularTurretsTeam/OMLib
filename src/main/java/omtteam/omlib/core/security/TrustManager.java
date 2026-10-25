package omtteam.omlib.core.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Owner and trust list of an owned block, and every permission decision based on them. Ported from the 1.20.1+
 * port, which took it from OMLib {@code TrustedPlayersManager}, {@code ITrustedPlayersManager} and
 * {@code PlayerUtil}, and from OMT {@code OMTUtil.canDamagePlayer} / {@code TurretTargetingUtils.validTarget}.
 * <p>
 * Rules:
 * <ul>
 *     <li>The owner always has {@link AccessLevel#ADMIN} rights. The owner is set by placing ({@link #setOwner}),
 *     removed by disowning ({@link #clearOwner}), and taken by the first player who claims an unowned block
 *     ({@link #claim}); the permission checks for these live with the callers.</li>
 *     <li>Players the owner shares ownership with ({@link SecurityEnvironment#isSharedOwner}) count as owner in
 *     every permission decision.</li>
 *     <li>Trusted players have the rights of their {@link AccessLevel} and the levels below it.</li>
 *     <li>Members of the owner's scoreboard team count as trusted at {@link AccessLevel#NONE} unless they are on
 *     the list with a higher level, so turrets do not shoot them.</li>
 *     <li>Operators may open every block and are never targeted if
 *     {@link SecurityEnvironment#operatorsCanAccessOwnedBlocks()} is set.</li>
 *     <li>Only admins (owner or {@link AccessLevel#ADMIN}) may change the trust list.</li>
 * </ul>
 * Methods taking a {@link PlayerIdentity} or {@link PlayerInfo} match players by UUID or, with
 * {@link SecurityEnvironment#offlineModeSupport()}, also by name; methods taking a {@link UUID} match exactly.
 */
public class TrustManager {
    private final SecurityEnvironment environment;
    private final Runnable onChange;
    private final AccessProfile profile;
    private final List<TrustedPlayer> trustedPlayers = new ArrayList<>();
    private PlayerIdentity owner;

    /** A manager that uses all access levels. */
    public TrustManager(SecurityEnvironment environment, Runnable onChange) {
        this(environment, onChange, AccessProfile.ALL);
    }

    /** @param profile the access levels this manager uses; stored and requested levels are clamped to it */
    public TrustManager(SecurityEnvironment environment, Runnable onChange, AccessProfile profile) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.onChange = Objects.requireNonNull(onChange, "onChange");
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    /** The access levels this manager uses. */
    public AccessProfile getProfile() {
        return profile;
    }

    // ---- State ----

    /** The owner, or {@code null} if the block has none (placed without a player, or disowned); anyone may claim it. */
    public PlayerIdentity getOwner() {
        return owner;
    }

    /** Sets the owner when the block is placed or loaded. Not reachable by players. */
    public void setOwner(PlayerIdentity owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
        trustedPlayers.removeIf(entry -> owner.is(entry.getUuid()));
        onChange.run();
    }

    /** Removes the owner and clears the trust list (disowning, the ownership wand's reset). */
    public void clearOwner() {
        owner = null;
        trustedPlayers.clear();
        onChange.run();
    }

    /**
     * Sets the owner only if there is none (claiming by right-click).
     *
     * @return true if the player is now the owner
     */
    public boolean claim(PlayerIdentity player) {
        Objects.requireNonNull(player, "player");
        if (owner != null) return false;
        setOwner(player);
        return true;
    }

    /** A read-only live view of the trusted players, without the owner. */
    public List<TrustedPlayer> getTrustedPlayers() {
        return Collections.unmodifiableList(trustedPlayers);
    }

    /** The trusted-list entry with exactly this UUID; empty for the owner, team members and strangers. */
    public Optional<TrustedPlayer> getTrustedPlayer(UUID uuid) {
        for (TrustedPlayer entry : trustedPlayers) {
            if (entry.getUuid().equals(uuid)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /** The trusted-list entry of this player (by UUID, or by name in offline mode). */
    public Optional<TrustedPlayer> getTrustedPlayer(PlayerIdentity player) {
        for (TrustedPlayer entry : trustedPlayers) {
            if (matches(entry.getIdentity(), player)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /**
     * Replaces the whole state without permission checks (loading from disk, memory cards). Entries for the owner
     * and duplicate UUIDs are skipped; levels outside the {@link #getProfile() profile} are clamped down.
     */
    public void load(PlayerIdentity owner, List<TrustedPlayer> entries) {
        this.owner = owner;
        trustedPlayers.clear();
        for (TrustedPlayer entry : entries) {
            boolean isOwner = owner != null && owner.is(entry.getUuid());
            if (!isOwner && !getTrustedPlayer(entry.getUuid()).isPresent()) {
                trustedPlayers.add(new TrustedPlayer(entry.getIdentity(), profile.clamp(entry.getAccessLevel())));
            }
        }
        onChange.run();
    }

    // ---- Queries ----

    /** Whether the UUID belongs to the owner itself (not a shared owner); false while the block has no owner. */
    public boolean isOwner(UUID uuid) {
        return owner != null && owner.is(uuid);
    }

    /** OMLib {@code isPlayerOwner}: the owner or a player the owner shares ownership with. */
    public boolean isOwner(PlayerInfo player) {
        return actsAsOwner(player.identity());
    }

    /**
     * Whether the player is in the owner's scoreboard team. Players without a team are never team members.
     */
    public boolean isTeamMember(PlayerIdentity player) {
        if (owner == null || actsAsOwner(player)) {
            return false;
        }
        Optional<String> ownerTeam = environment.getTeam(owner.uuid(), owner.name());
        if (!ownerTeam.isPresent() || ownerTeam.get().isEmpty()) {
            return false;
        }
        return ownerTeam.equals(environment.getTeam(player.uuid(), player.name()));
    }

    /**
     * Trust level of a player, ignoring ownership and operator rights: the list entry, otherwise
     * {@link AccessLevel#NONE} for members of the owner's team, otherwise empty.
     */
    public Optional<AccessLevel> getTrustLevel(PlayerIdentity player) {
        Optional<TrustedPlayer> entry = getTrustedPlayer(player);
        if (entry.isPresent()) {
            return Optional.of(entry.get().getAccessLevel());
        }
        return isTeamMember(player) ? Optional.of(AccessLevel.NONE) : Optional.<AccessLevel>empty();
    }

    /**
     * Effective access level of a player (OMLib {@code PlayerUtil.getPlayerAccessLevel}); empty if the player has
     * no relation to the block at all. Owner and shared owners: {@link AccessLevel#ADMIN}; operators (if allowed): at
     * least {@link AccessLevel#OPEN_GUI}.
     */
    public Optional<AccessLevel> getAccessLevel(PlayerInfo player) {
        if (isOwner(player)) {
            return Optional.of(AccessLevel.ADMIN);
        }
        Optional<AccessLevel> level = getTrustLevel(player.identity());
        boolean belowOpenGui = !level.isPresent() || !level.get().isAtLeast(AccessLevel.OPEN_GUI);
        if (isPrivilegedOperator(player) && belowOpenGui) {
            return Optional.of(profile.clamp(AccessLevel.OPEN_GUI));
        }
        return level;
    }

    /** Whether the player's {@link #getAccessLevel effective level} is at least {@code required}. */
    public boolean hasAccess(PlayerInfo player, AccessLevel required) {
        Optional<AccessLevel> level = getAccessLevel(player);
        return level.isPresent() && level.get().isAtLeast(required);
    }

    /** OMLib {@code canPlayerAccessBlock}: open the GUI and inventory. */
    public boolean canOpenGui(PlayerInfo player) {
        return hasAccess(player, AccessLevel.OPEN_GUI);
    }

    /** OMLib {@code canPlayerChangeSetting}: targeting toggles, targeting type, multi-targeting. */
    public boolean canChangeSettings(PlayerInfo player) {
        return hasAccess(player, AccessLevel.CHANGE_SETTINGS);
    }

    /**
     * OMLib {@code isPlayerAdmin}: trust list, redstone mode, range, light settings, dropping the block.
     */
    public boolean isAdmin(PlayerInfo player) {
        return hasAccess(player, AccessLevel.ADMIN);
    }

    /** Owner-only actions (camo handling, removing attachments). */
    public boolean canPerformOwnerAction(PlayerInfo player) {
        return isOwner(player);
    }

    /**
     * Whether turrets must never select this player as a target (OMT {@code TurretTargetingUtils.validTarget}):
     * owner, trusted (any level, including team members), privileged operators and players that may not be
     * damaged.
     */
    public boolean isProtectedFromTargeting(PlayerInfo player) {
        return isOwnerOrTrusted(player.identity()) || isPrivilegedOperator(player) || !canDamagePlayer(player.identity());
    }

    /**
     * Whether a player (or a pet owned by that player) counts as friendly: owner or trusted at any level
     * (OMT pet check in {@code validTarget}). Works for offline players.
     */
    public boolean isOwnerOrTrusted(PlayerIdentity player) {
        return actsAsOwner(player) || getTrustLevel(player).isPresent();
    }

    /**
     * Whether a projectile of this block may hurt the player (OMT {@code OMTUtil.canDamagePlayer}). The owner and
     * the owner's team are never hurt; listed trusted players only if {@code turretDamageTrustedPlayers} is set.
     */
    public boolean canDamagePlayer(PlayerIdentity player) {
        if (actsAsOwner(player) || isTeamMember(player)) {
            return false;
        }
        return environment.turretsDamageTrustedPlayers() || !getTrustedPlayer(player).isPresent();
    }

    private boolean isPrivilegedOperator(PlayerInfo player) {
        return player.operator() && environment.operatorsCanAccessOwnedBlocks();
    }

    /** The owner or a shared owner of it. */
    private boolean actsAsOwner(PlayerIdentity player) {
        return owner != null && (matches(owner, player) || environment.isSharedOwner(owner, player));
    }

    /** Whether a stored identity is this player: same UUID, or in offline mode the same name (ignoring case). */
    private boolean matches(PlayerIdentity stored, PlayerIdentity player) {
        return stored.is(player.uuid())
                || (environment.offlineModeSupport() && stored.name().equalsIgnoreCase(player.name()));
    }

    // ---- Changes by players (all require admin rights) ----

    /**
     * Adds a player at {@link AccessLevel#NONE} (OMLib {@code addTrustedPlayer}). The caller resolves the name to a
     * UUID first.
     */
    public TrustResult addTrustedPlayer(PlayerInfo actor, PlayerIdentity player) {
        if (!isAdmin(actor)) {
            return TrustResult.NO_PERMISSION;
        }
        if (owner != null && matches(owner, player)) {
            return TrustResult.IS_OWNER;
        }
        if (getTrustedPlayer(player).isPresent()) {
            return TrustResult.ALREADY_TRUSTED;
        }
        trustedPlayers.add(new TrustedPlayer(player, AccessLevel.NONE));
        onChange.run();
        return TrustResult.SUCCESS;
    }

    /** Removes a player from the trusted list; {@link TrustResult#NOT_FOUND} if not on it. */
    public TrustResult removeTrustedPlayer(PlayerInfo actor, UUID player) {
        if (!isAdmin(actor)) {
            return TrustResult.NO_PERMISSION;
        }
        if (!trustedPlayers.removeIf(entry -> entry.getUuid().equals(player))) {
            return TrustResult.NOT_FOUND;
        }
        onChange.run();
        return TrustResult.SUCCESS;
    }

    /**
     * Raises ({@code +1}) or lowers ({@code -1}) the level of a trusted player by one step of the profile (OMLib
     * {@code changePermission(String, int)}).
     */
    public TrustResult changeAccessLevel(PlayerInfo actor, UUID player, int delta) {
        if (!isAdmin(actor)) {
            return TrustResult.NO_PERMISSION;
        }
        Optional<TrustedPlayer> entry = getTrustedPlayer(player);
        if (!entry.isPresent()) {
            return TrustResult.NOT_FOUND;
        }
        AccessLevel newLevel = profile.step(entry.get().getAccessLevel(), delta);
        if (newLevel == null) {
            return TrustResult.OUT_OF_RANGE;
        }
        entry.get().setAccessLevel(newLevel);
        onChange.run();
        return TrustResult.SUCCESS;
    }

    /**
     * Sets the level of a trusted player directly (unlike {@link #changeAccessLevel}, which steps);
     * {@link TrustResult#OUT_OF_RANGE} if the level is not in the profile.
     */
    public TrustResult setAccessLevel(PlayerInfo actor, UUID player, AccessLevel level) {
        if (!isAdmin(actor)) {
            return TrustResult.NO_PERMISSION;
        }
        if (!profile.allows(level)) {
            return TrustResult.OUT_OF_RANGE;
        }
        Optional<TrustedPlayer> entry = getTrustedPlayer(player);
        if (!entry.isPresent()) {
            return TrustResult.NOT_FOUND;
        }
        entry.get().setAccessLevel(level);
        onChange.run();
        return TrustResult.SUCCESS;
    }

    /**
     * Updates the stored display name of the owner or a trusted player after a rename. Names are display-only.
     */
    public void refreshName(PlayerInfo player) {
        boolean changed = false;
        if (owner != null && owner.is(player.uuid()) && !owner.name().equals(player.name())) {
            owner = owner.withName(player.name());
            changed = true;
        }
        Optional<TrustedPlayer> entry = getTrustedPlayer(player.uuid());
        if (entry.isPresent() && !entry.get().getName().equals(player.name())) {
            entry.get().setName(player.name());
            changed = true;
        }
        if (changed) {
            onChange.run();
        }
    }
}
