package omtteam.omlib.core.card;

import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.core.machine.MachineMode;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.AccessProfile;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.PlayerInfo;
import omtteam.omlib.core.security.SecurityEnvironment;
import omtteam.omlib.core.security.TrustManager;
import omtteam.omlib.core.security.TrustResult;
import omtteam.omlib.core.security.TrustedPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryCardRulesTest {
    private static final PlayerInfo OWNER = player("Owner");
    private static final PlayerInfo ADMIN = player("Admin");
    private static final PlayerInfo SETTINGS = player("Settings");
    private static final PlayerInfo STRANGER = player("Stranger");

    private static final SecurityEnvironment NO_TEAMS = new SecurityEnvironment() {
        @Override
        public Optional<String> getTeam(UUID uuid, String name) {
            return Optional.empty();
        }

        @Override
        public boolean operatorsCanAccessOwnedBlocks() {
            return false;
        }

        @Override
        public boolean turretsDamageTrustedPlayers() {
            return false;
        }

        @Override
        public boolean isSharedOwner(PlayerIdentity owner, PlayerIdentity player) {
            return false;
        }

        @Override
        public boolean offlineModeSupport() {
            return false;
        }
    };

    private TrustManager base;

    private static PlayerInfo player(String name) {
        return new PlayerInfo(UUID.nameUUIDFromBytes(name.getBytes()), name, false);
    }

    private static MemoryCardData.BaseSettings baseSettings() {
        return new MemoryCardData.BaseSettings(MachineMode.INVERTED, false, true, true, false, 10);
    }

    @BeforeEach
    void setUp() {
        base = new TrustManager(NO_TEAMS, () -> {
        });
        base.setOwner(OWNER.identity());
        assertEquals(TrustResult.SUCCESS, base.addTrustedPlayer(OWNER, ADMIN.identity()));
        assertEquals(TrustResult.SUCCESS, base.setAccessLevel(OWNER, ADMIN.uuid(), AccessLevel.ADMIN));
        assertEquals(TrustResult.SUCCESS, base.addTrustedPlayer(OWNER, SETTINGS.identity()));
        assertEquals(TrustResult.SUCCESS, base.setAccessLevel(OWNER, SETTINGS.uuid(), AccessLevel.CHANGE_SETTINGS));
    }

    @Test
    void onlyAdminsCopyAndPaste() {
        for (PlayerInfo admin : Arrays.asList(OWNER, ADMIN)) {
            assertTrue(MemoryCardRules.canCopy(base, admin));
            assertTrue(MemoryCardRules.canPaste(base, admin));
        }
        for (PlayerInfo other : Arrays.asList(SETTINGS, STRANGER)) {
            assertFalse(MemoryCardRules.canCopy(base, other));
            assertFalse(MemoryCardRules.canPaste(base, other));
        }
    }

    @Test
    void cardPartsMustFitTheSource() {
        MemoryCardData.TrustSettings trust = new MemoryCardData.TrustSettings(Collections.<TrustedPlayer>emptyList());
        MemoryCardData.CamoSettings camo = new MemoryCardData.CamoSettings("minecraft:stone@0", CamoLight.DEFAULT);
        assertThrows(IllegalArgumentException.class,
                () -> new MemoryCardData(MemoryCardData.Source.TURRET_BASE, null, trust, null));
        assertThrows(IllegalArgumentException.class,
                () -> new MemoryCardData(MemoryCardData.Source.CAMO_TRAP, baseSettings(), trust, camo));
        assertThrows(IllegalArgumentException.class,
                () -> new MemoryCardData(MemoryCardData.Source.CAMO_TRAP, null, trust, null));
        new MemoryCardData(MemoryCardData.Source.TURRET_BASE, baseSettings(), trust, null);
        new MemoryCardData(MemoryCardData.Source.CAMO_TRAP, null, trust, camo);
    }

    @Test
    void pastingATrustListKeepsTheOwnerOfTheTarget() {
        MemoryCardData.TrustSettings card = MemoryCardRules.captureTrust(base);
        TrustManager other = new TrustManager(NO_TEAMS, () -> {
        });
        other.setOwner(ADMIN.identity());
        MemoryCardRules.applyTrust(card, other);
        assertEquals(ADMIN.identity(), other.getOwner());
        assertFalse(other.getTrustedPlayer(ADMIN.uuid()).isPresent(), "the owner is never on the list");
        assertEquals(AccessLevel.CHANGE_SETTINGS, other.getTrustedPlayer(SETTINGS.uuid()).get().getAccessLevel());
    }

    @Test
    void aTrapClampsTheLevelsOfACardFromABase() {
        TrustManager trap = new TrustManager(NO_TEAMS, () -> {
        }, AccessProfile.TRUSTED_AND_ADMIN);
        trap.setOwner(OWNER.identity());
        MemoryCardRules.applyTrust(MemoryCardRules.captureTrust(base), trap);
        assertEquals(AccessLevel.ADMIN, trap.getTrustedPlayer(ADMIN.uuid()).get().getAccessLevel());
        assertEquals(AccessLevel.NONE, trap.getTrustedPlayer(SETTINGS.uuid()).get().getAccessLevel());
    }

    @Test
    void theCardKeepsItsOwnCopyOfTheList() {
        MemoryCardData.TrustSettings card = MemoryCardRules.captureTrust(base);
        assertEquals(TrustResult.SUCCESS, base.removeTrustedPlayer(OWNER, ADMIN.uuid()));
        assertEquals(2, card.trustedPlayers().size());
    }
}
