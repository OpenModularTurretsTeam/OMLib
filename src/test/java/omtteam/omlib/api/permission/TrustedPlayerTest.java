package omtteam.omlib.api.permission;

import omtteam.omlib.config.OMConfig;
import omtteam.omlib.util.player.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TrustedPlayerTest {
    private final TrustedPlayer steve = new TrustedPlayer(new Player(UUID.randomUUID(), "Steve"), EnumAccessLevel.ADMIN);
    private final TrustedPlayer otherSteve = new TrustedPlayer(new Player(UUID.randomUUID(), "Steve"),
            EnumAccessLevel.ADMIN);

    @AfterEach
    void tearDown() {
        OMConfig.GENERAL.offlineModeSupport = false;
    }

    @Test
    void onlineTheUuidCounts() {
        assertNotEquals(steve, otherSteve);
        assertNotEquals(steve, new TrustedPlayer(steve.getPlayer(), EnumAccessLevel.OPEN_GUI), "and the level");
    }

    @Test
    void offlineOnlyNameAndLevelCount() {
        OMConfig.GENERAL.offlineModeSupport = true;
        assertEquals(steve, otherSteve);
        assertEquals(steve.hashCode(), otherSteve.hashCode());
    }
}
