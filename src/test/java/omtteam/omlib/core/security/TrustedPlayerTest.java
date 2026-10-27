package omtteam.omlib.core.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrustedPlayerTest {
    private static final PlayerIdentity ALICE = new PlayerIdentity(UUID.nameUUIDFromBytes("Alice".getBytes()), "Alice");

    @Test
    void identityAndLevelAreRequired() {
        assertThrows(NullPointerException.class, () -> new TrustedPlayer(null, AccessLevel.NONE));
        assertThrows(NullPointerException.class, () -> new TrustedPlayer(ALICE, null));
        TrustedPlayer entry = new TrustedPlayer(ALICE, AccessLevel.NONE);
        assertThrows(NullPointerException.class, () -> entry.setAccessLevel(null));
    }

    @Test
    void gettersReadTheIdentity() {
        TrustedPlayer entry = new TrustedPlayer(ALICE, AccessLevel.OPEN_GUI);
        assertEquals(ALICE.uuid(), entry.getUuid());
        assertEquals("Alice", entry.getName());
        assertEquals(ALICE, entry.getIdentity());
        assertEquals(AccessLevel.OPEN_GUI, entry.getAccessLevel());
    }

    @Test
    void renamingKeepsTheUuid() {
        TrustedPlayer entry = new TrustedPlayer(ALICE, AccessLevel.NONE);
        entry.setName("Renamed");
        assertEquals("Renamed", entry.getName());
        assertEquals(ALICE.uuid(), entry.getUuid());
    }

    @Test
    void aCopyDoesNotChangeWithTheOriginal() {
        TrustedPlayer entry = new TrustedPlayer(ALICE, AccessLevel.NONE);
        TrustedPlayer copy = entry.copy();
        entry.setAccessLevel(AccessLevel.ADMIN);
        entry.setName("Renamed");
        assertEquals(AccessLevel.NONE, copy.getAccessLevel());
        assertEquals("Alice", copy.getName());
    }
}
