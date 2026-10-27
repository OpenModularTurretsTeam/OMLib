package omtteam.omlib.core.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerIdentityTest {
    private static final UUID ALICE = UUID.nameUUIDFromBytes("Alice".getBytes());

    @Test
    void uuidAndNameAreRequired() {
        assertThrows(NullPointerException.class, () -> new PlayerIdentity(null, "Alice"));
        assertThrows(NullPointerException.class, () -> new PlayerIdentity(ALICE, null));
    }

    @Test
    void theUuidIsTheIdentityNotTheName() {
        PlayerIdentity alice = new PlayerIdentity(ALICE, "Alice");
        assertTrue(alice.is(ALICE));
        assertTrue(alice.withName("Renamed").is(ALICE));
        assertFalse(alice.is(UUID.nameUUIDFromBytes("Bob".getBytes())));
    }

    @Test
    void withNameKeepsTheUuid() {
        PlayerIdentity renamed = new PlayerIdentity(ALICE, "Alice").withName("Renamed");
        assertEquals(new PlayerIdentity(ALICE, "Renamed"), renamed);
    }

    @Test
    void playerInfoGivesTheIdentityWithoutTheOperatorFlag() {
        assertEquals(new PlayerIdentity(ALICE, "Alice"), new PlayerInfo(ALICE, "Alice", true).identity());
    }
}
