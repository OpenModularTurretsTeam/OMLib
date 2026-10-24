package omtteam.omlib.api.tile;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.api.permission.EnumAccessLevel;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.api.permission.TrustedPlayersManager;
import omtteam.omlib.config.OMConfig;
import omtteam.omlib.util.RandomUtil;
import omtteam.omlib.util.player.Player;
import net.minecraft.nbt.NBTTagList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustedPlayersManagerTest {
    private TrustedPlayersManager tpm = new TrustedPlayersManager(null);
    private final Player owner = new Player(UUID.randomUUID(), "Owner");
    private final Player alice = new Player(UUID.randomUUID(), "Alice");
    private final Player bob = new Player(UUID.randomUUID(), "Bob");

    @AfterEach
    void tearDown() {
        OMConfig.GENERAL.offlineModeSupport = false;
    }

    @Test
    void testTrustedPlayerAdditionAndRemoval() {
        Player player = new Player(UUID.randomUUID(), "test", "test");
        OMConfig.GENERAL.offlineModeSupport = true;
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.getName()), "trustedPlayer needs to be gettable by name");
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.getUuid()), "trustedPlayer needs to be gettable by uuid");
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player), "trustedPlayer needs to be gettable by player");
            tpm = new TrustedPlayersManager(null); // getTrustedPlayers() is a copy since 4.0.0
        }
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            tpm.removeTrustedPlayer(player.getName());
            assertNull(tpm.getTrustedPlayer(player), "removed trustedPlayer needs to be null");
            tpm = new TrustedPlayersManager(null); // getTrustedPlayers() is a copy since 4.0.0
        }
        OMConfig.GENERAL.offlineModeSupport = false;
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.getName()), "trustedPlayer needs to be gettable by name");
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player), "trustedPlayer needs to be gettable by player");
            tpm = new TrustedPlayersManager(null); // getTrustedPlayers() is a copy since 4.0.0
        }
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            tpm.removeTrustedPlayer(player.getName());
            assertNull(tpm.getTrustedPlayer(player), "removed trustedPlayer needs to be null");
            tpm = new TrustedPlayersManager(null); // getTrustedPlayers() is a copy since 4.0.0
        }
    }

    @Test
    void checkNBT() {
        tpm = new TrustedPlayersManager(null);
        RandomUtil.init();
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            Player player = new Player(UUID.randomUUID(), RandomUtil.random.nextLong() + "", "test");
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tpm.writeToNBT(tag);
        TrustedPlayersManager that = new TrustedPlayersManager(null);
        that.readFromNBT(tag);
        assertEquals(tpm.getTrustedPlayers(), that.getTrustedPlayers());
    }

    @Test
    void theOwnerComesFromTheConstructor() {
        TrustedPlayersManager manager = new TrustedPlayersManager(owner);
        assertEquals(owner, manager.getOwner());
        assertEquals(owner.getUuid(), manager.core().getOwner().uuid());
        assertFalse(manager.addTrustedPlayer(new TrustedPlayer(owner, EnumAccessLevel.ADMIN)),
                "the owner is not added to the own list");
    }

    @Test
    void inOfflineModeTheOwnersNameCannotBeTrusted() {
        TrustedPlayersManager manager = new TrustedPlayersManager(owner);
        Player sameName = new Player(UUID.randomUUID(), "Owner");
        assertTrue(manager.addTrustedPlayer(new TrustedPlayer(sameName, EnumAccessLevel.ADMIN)),
                "online the UUID decides");
        OMConfig.GENERAL.offlineModeSupport = true;
        TrustedPlayersManager offline = new TrustedPlayersManager(owner);
        assertFalse(offline.addTrustedPlayer(new TrustedPlayer(sameName, EnumAccessLevel.ADMIN)));
    }

    @Test
    void listsWhatWasAdded() {
        TrustedPlayersManager manager = new TrustedPlayersManager(owner);
        assertTrue(manager.addTrustedPlayer(new TrustedPlayer(alice, EnumAccessLevel.ADMIN)));
        assertTrue(manager.addTrustedPlayer(new TrustedPlayer(bob, EnumAccessLevel.OPEN_GUI)));
        assertFalse(manager.addTrustedPlayer(new TrustedPlayer(alice, EnumAccessLevel.NONE)), "no duplicates");

        List<TrustedPlayer> players = manager.getTrustedPlayers();
        assertEquals(2, players.size());
        assertEquals("Alice", players.get(0).getName());
        assertEquals(alice.getUuid(), players.get(0).getUuid());
        assertEquals(EnumAccessLevel.ADMIN, players.get(0).getAccessLevel());
        assertEquals(EnumAccessLevel.OPEN_GUI, players.get(1).getAccessLevel());
    }

    @Test
    void removingOnePlayerKeepsTheOthers() {
        TrustedPlayersManager manager = new TrustedPlayersManager(owner);
        manager.addTrustedPlayer(new TrustedPlayer(alice, EnumAccessLevel.ADMIN));
        manager.addTrustedPlayer(new TrustedPlayer(bob, EnumAccessLevel.ADMIN));

        assertTrue(manager.removeTrustedPlayer("alice"), "names in any case");
        assertFalse(manager.removeTrustedPlayer("alice"));

        assertEquals(1, manager.getTrustedPlayers().size());
        assertEquals("Bob", manager.getTrustedPlayers().get(0).getName());
    }

    @Test
    void nbtKeepsUuidsAndLevels() {
        TrustedPlayersManager manager = new TrustedPlayersManager(owner);
        manager.addTrustedPlayer(new TrustedPlayer(alice, EnumAccessLevel.CHANGE_SETTINGS));
        manager.addTrustedPlayer(new TrustedPlayer(bob, EnumAccessLevel.OPEN_GUI));
        NBTTagCompound tag = new NBTTagCompound();
        assertTrue(tag == manager.writeToNBT(tag), "writes into the given tag");
        assertEquals(alice.getUuid().toString(),
                tag.getTagList("trustedPlayers", 10).getCompoundTagAt(0).getString("UUID"));

        TrustedPlayersManager loaded = new TrustedPlayersManager(owner);
        loaded.readFromNBT(tag);

        assertEquals(2, loaded.getTrustedPlayers().size());
        assertEquals(EnumAccessLevel.CHANGE_SETTINGS, loaded.getTrustedPlayer(alice.getUuid()).getAccessLevel());
        assertEquals(EnumAccessLevel.OPEN_GUI, loaded.getTrustedPlayer(bob.getUuid()).getAccessLevel());
    }

    /** The level of a saved entry with this {@code accessLevel} ordinal, after loading. */
    private EnumAccessLevel loadLevel(int ordinal) {
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("name", "Alice");
        entry.setInteger("accessLevel", ordinal);
        entry.setString("UUID", alice.getUuid().toString());
        NBTTagList list = new NBTTagList();
        list.appendTag(entry);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("trustedPlayers", list);
        TrustedPlayersManager loaded = new TrustedPlayersManager(owner);
        loaded.readFromNBT(tag);
        return loaded.getTrustedPlayer(alice.getUuid()).getAccessLevel();
    }

    @Test
    void savedAccessLevelsAreReadByOrdinal() {
        EnumAccessLevel[] levels = EnumAccessLevel.values();
        assertEquals(levels[0], loadLevel(0));
        assertEquals(levels[levels.length - 1], loadLevel(levels.length - 1));
        assertEquals(EnumAccessLevel.NONE, loadLevel(-1));
        assertEquals(EnumAccessLevel.NONE, loadLevel(levels.length));
        assertEquals(EnumAccessLevel.NONE, loadLevel(99));
    }
}
