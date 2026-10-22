package omtteam.omlib.core.card;

import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.core.machine.MachineMode;
import omtteam.omlib.core.security.TrustedPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * What a turret memory card stores (OMT {@code UsableMetaItem} meta 2, 1.20.1+ {@code MemoryCardData}): the settings
 * of a turret base or of a camouflage trap, to be written to other ones.
 * <p>
 * A card from a base holds its {@link BaseSettings}, the {@link TrustSettings} and, if camouflage is enabled, its
 * {@link CamoSettings}; a card from a trap holds the trust and camouflage settings.
 */
public final class MemoryCardData {
    public enum Source {
        TURRET_BASE,
        CAMO_TRAP
    }

    private final Source source;
    @Nullable
    private final BaseSettings base;
    private final TrustSettings trust;
    @Nullable
    private final CamoSettings camo;

    /**
     * @param base  targeting and mode; present exactly on cards from a turret base
     * @param camo  disguise and light; null on cards from a base without camouflage, present on trap cards
     * @throws IllegalArgumentException if the parts do not fit the source
     */
    public MemoryCardData(Source source, @Nullable BaseSettings base, TrustSettings trust,
                          @Nullable CamoSettings camo) {
        this.source = Objects.requireNonNull(source, "source");
        this.trust = Objects.requireNonNull(trust, "trust");
        if ((source == Source.TURRET_BASE) == (base == null)) {
            throw new IllegalArgumentException("Base settings belong to cards from a turret base only");
        }
        if (source == Source.CAMO_TRAP && camo == null) {
            throw new IllegalArgumentException("A card from a trap stores its camouflage");
        }
        this.base = base;
        this.camo = camo;
    }

    public Source source() {
        return source;
    }

    @Nullable
    public BaseSettings base() {
        return base;
    }

    public TrustSettings trust() {
        return trust;
    }

    @Nullable
    public CamoSettings camo() {
        return camo;
    }

    /** Settings of a turret base; {@code range} is the chosen range, the target base clamps it to its own maximum. */
    public static final class BaseSettings {
        private final MachineMode mode;
        private final boolean multiTargeting;
        private final boolean targetPlayers;
        private final boolean targetMobs;
        private final boolean targetPassive;
        private final int range;

        public BaseSettings(MachineMode mode, boolean multiTargeting, boolean targetPlayers, boolean targetMobs,
                            boolean targetPassive, int range) {
            this.mode = Objects.requireNonNull(mode, "mode");
            this.multiTargeting = multiTargeting;
            this.targetPlayers = targetPlayers;
            this.targetMobs = targetMobs;
            this.targetPassive = targetPassive;
            this.range = range;
        }

        public MachineMode mode() {
            return mode;
        }

        public boolean multiTargeting() {
            return multiTargeting;
        }

        public boolean targetPlayers() {
            return targetPlayers;
        }

        public boolean targetMobs() {
            return targetMobs;
        }

        public boolean targetPassive() {
            return targetPassive;
        }

        public int range() {
            return range;
        }
    }

    /**
     * The trust list of a base or trap, without the owner. The target clamps the levels to the ones it uses (a trap
     * only knows "trusted" and "admin").
     */
    public static final class TrustSettings {
        private final List<TrustedPlayer> trustedPlayers;

        public TrustSettings(List<TrustedPlayer> trustedPlayers) {
            List<TrustedPlayer> copies = new ArrayList<>();
            for (TrustedPlayer player : trustedPlayers) {
                copies.add(player.copy());
            }
            this.trustedPlayers = Collections.unmodifiableList(copies);
        }

        public List<TrustedPlayer> trustedPlayers() {
            return trustedPlayers;
        }
    }

    /**
     * Camouflage settings: the disguise as registry id and metadata ({@code minecraft:stone@1}), null for none, and the
     * light, null if the source has no adjustable light.
     */
    public static final class CamoSettings {
        @Nullable
        private final String disguise;
        @Nullable
        private final CamoLight light;

        public CamoSettings(@Nullable String disguise, @Nullable CamoLight light) {
            this.disguise = disguise;
            this.light = light;
        }

        @Nullable
        public String disguise() {
            return disguise;
        }

        @Nullable
        public CamoLight light() {
            return light;
        }
    }
}
