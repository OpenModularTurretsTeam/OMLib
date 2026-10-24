package omtteam.omlib.util.player;

import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.config.OMConfig;
import omtteam.omlib.core.security.PlayerIdentity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OMLibSecurityEnvironmentTest {
    private static final OMLibSecurityEnvironment ENVIRONMENT = OMLibSecurityEnvironment.INSTANCE;
    private final PlayerIdentity owner = new PlayerIdentity(UUID.randomUUID(), "Owner");
    private final PlayerIdentity friend = new PlayerIdentity(UUID.randomUUID(), "Friend");

    @AfterEach
    void tearDown() {
        OMConfig.GENERAL.offlineModeSupport = false;
        OwnerShareRegister.instance.setOwnerShareMap(new HashMap<>());
    }

    @Test
    void sharedOwnersComeFromTheShareRegister() {
        HashMap<Player, ArrayList<Player>> shares = new HashMap<>();
        shares.put(OMLibSecurityEnvironment.toPlayer(owner),
                new ArrayList<>(Collections.singletonList(OMLibSecurityEnvironment.toPlayer(friend))));
        OwnerShareRegister.instance.setOwnerShareMap(shares);

        assertTrue(ENVIRONMENT.isSharedOwner(owner, friend));
        assertFalse(ENVIRONMENT.isSharedOwner(friend, owner));
    }

    @Test
    void offlineModeFollowsTheConfig() {
        assertFalse(ENVIRONMENT.offlineModeSupport());
        OMConfig.GENERAL.offlineModeSupport = true;
        assertTrue(ENVIRONMENT.offlineModeSupport());
    }

    @Test
    void convertsBetweenPlayersAndIdentities() {
        Player player = OMLibSecurityEnvironment.toPlayer(owner);
        assertEquals(owner.uuid(), player.getUuid());
        assertEquals("Owner", player.getName());
        assertEquals("", player.getTeamName());
        assertEquals(owner, OMLibSecurityEnvironment.toIdentity(player));
    }

    @Test
    void playersWithoutUuidGetTheirOfflineUuid() {
        PlayerIdentity identity = OMLibSecurityEnvironment.toIdentity(new Player(null, "Legacy"));
        assertEquals(OMLibSecurityEnvironment.offlineUuid("Legacy"), identity.uuid());
    }
}
