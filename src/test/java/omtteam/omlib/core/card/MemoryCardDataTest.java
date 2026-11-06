package omtteam.omlib.core.card;

import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.core.machine.MachineMode;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.TrustedPlayer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The card data keeps what the game layer writes to and reads from the card's NBT. */
class MemoryCardDataTest {
    private static final List<TrustedPlayer> TRUSTED = Collections.singletonList(
            new TrustedPlayer(new PlayerIdentity(UUID.fromString("00000000-0000-0000-0000-00000000000a"), "Alice"),
                    AccessLevel.CHANGE_SETTINGS));

    @Test
    void baseCardKeepsItsParts() {
        MemoryCardData.BaseSettings base = new MemoryCardData.BaseSettings(MachineMode.ALWAYS_ON, true, false, true,
                false, 7);
        MemoryCardData.TrustSettings trust = new MemoryCardData.TrustSettings(TRUSTED);
        MemoryCardData card = new MemoryCardData(MemoryCardData.Source.TURRET_BASE, base, trust, null);

        assertEquals(MemoryCardData.Source.TURRET_BASE, card.source());
        assertSame(base, card.base());
        assertSame(trust, card.trust());
        assertNull(card.camo());
        TrustedPlayer kept = card.trust().trustedPlayers().get(0);
        assertEquals(1, card.trust().trustedPlayers().size());
        assertEquals(TRUSTED.get(0).getUuid(), kept.getUuid());
        assertEquals(AccessLevel.CHANGE_SETTINGS, kept.getAccessLevel());
    }

    @Test
    void baseSettingsKeepEachFlag() {
        MemoryCardData.BaseSettings base = new MemoryCardData.BaseSettings(MachineMode.INVERTED, true, false, true,
                false, 7);
        assertEquals(MachineMode.INVERTED, base.mode());
        assertTrue(base.multiTargeting());
        assertFalse(base.targetPlayers());
        assertTrue(base.targetMobs());
        assertFalse(base.targetPassive());
        assertEquals(7, base.range());

        MemoryCardData.BaseSettings flipped = new MemoryCardData.BaseSettings(MachineMode.INVERTED, false, true,
                false, true, 7);
        assertFalse(flipped.multiTargeting());
        assertTrue(flipped.targetPlayers());
        assertFalse(flipped.targetMobs());
        assertTrue(flipped.targetPassive());
    }

    @Test
    void trapCardKeepsItsCamouflage() {
        CamoLight light = new CamoLight(9, 3);
        MemoryCardData.CamoSettings camo = new MemoryCardData.CamoSettings("minecraft:stone", light);
        MemoryCardData card = new MemoryCardData(MemoryCardData.Source.CAMO_TRAP, null,
                new MemoryCardData.TrustSettings(TRUSTED), camo);

        assertSame(camo, card.camo());
        assertEquals("minecraft:stone", card.camo().disguise());
        assertSame(light, card.camo().light());
        assertNull(card.base());
    }
}
