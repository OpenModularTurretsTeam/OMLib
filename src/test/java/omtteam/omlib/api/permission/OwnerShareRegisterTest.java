package omtteam.omlib.api.permission;

import net.minecraft.nbt.NBTBase;
import omtteam.omlib.util.player.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerShareRegisterTest {
    private static final OwnerShareRegister REGISTER = OwnerShareRegister.instance;
    private final Player owner = new Player(UUID.randomUUID(), "Owner", "red");
    private final Player friend = new Player(UUID.randomUUID(), "Friend");
    private final Player other = new Player(UUID.randomUUID(), "Other");

    @AfterEach
    void tearDown() {
        REGISTER.setOwnerShareMap(new HashMap<>());
    }

    private static NBTBase saved(Player owner, Player... shared) {
        HashMap<Player, ArrayList<Player>> map = new HashMap<>();
        map.put(owner, new ArrayList<>(Arrays.asList(shared)));
        REGISTER.setOwnerShareMap(map);
        return REGISTER.serializeNBT();
    }

    @Test
    void theShareListsSurviveSaving() {
        NBTBase nbt = saved(owner, friend, other);
        REGISTER.setOwnerShareMap(new HashMap<>());

        REGISTER.deserializeNBT(nbt);

        assertEquals(Arrays.asList(friend, other), REGISTER.getOwnerShareMap().get(owner));
        assertTrue(REGISTER.isPlayerSharedOwner(owner, friend));
        assertFalse(REGISTER.isPlayerSharedOwner(friend, owner), "sharing is one way");
        Player loadedOwner = REGISTER.getOwnerShareMap().keySet().iterator().next();
        assertEquals("Owner", loadedOwner.getName());
        assertEquals("red", loadedOwner.getTeamName());
    }

    @Test
    void loadingAWorldDropsTheListsOfTheWorldBefore() {
        NBTBase secondWorld = saved(other, friend);
        saved(owner, friend);

        REGISTER.deserializeNBT(secondWorld);

        assertEquals(1, REGISTER.getOwnerShareMap().size());
        assertFalse(REGISTER.isPlayerSharedOwner(owner, friend));
        assertTrue(REGISTER.isPlayerSharedOwner(other, friend));
    }
}
