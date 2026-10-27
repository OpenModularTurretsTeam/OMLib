package omtteam.omlib.security;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import omtteam.omlib.compat.nbt.NbtCompat;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.SecurityEnvironment;
import omtteam.omlib.core.security.TrustManager;
import omtteam.omlib.core.security.TrustedPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityNbtTest {
    private static final PlayerIdentity OWNER = player("Owner");
    private static final PlayerIdentity ALICE = player("Alice");
    private static final PlayerIdentity BOB = player("Bob");

    private TrustManager manager;

    private static PlayerIdentity player(String name) {
        return new PlayerIdentity(UUID.nameUUIDFromBytes(name.getBytes()), name);
    }

    private static TrustManager newManager() {
        return new TrustManager(new SecurityEnvironment() {
            @Override
            public Optional<String> getTeam(UUID uuid, String name) {
                return Optional.empty();
            }

            @Override
            public boolean operatorsCanAccessOwnedBlocks() {
                return false;
            }

            @Override
            public boolean damageTrustedPlayers() {
                return false;
            }
        }, () -> {
        });
    }

    @BeforeEach
    void setUp() {
        manager = newManager();
        manager.load(OWNER, List.of(new TrustedPlayer(ALICE, AccessLevel.ADMIN), new TrustedPlayer(BOB, AccessLevel.OPEN_GUI)));
    }

    private static CompoundTag saved(TrustManager manager) {
        CompoundTag tag = new CompoundTag();
        SecurityNbt.save(manager, tag);
        return tag;
    }

    @Test
    void ownerAndTrustListSurviveSaveAndLoad() {
        TrustManager loaded = newManager();
        SecurityNbt.load(loaded, saved(manager));
        assertEquals(OWNER, loaded.getOwner());
        assertEquals(2, loaded.getTrustedPlayers().size());
        assertEquals(ALICE, loaded.getTrustedPlayers().get(0).getIdentity());
        assertEquals(AccessLevel.ADMIN, loaded.getTrustedPlayers().get(0).getAccessLevel());
        assertEquals(BOB, loaded.getTrustedPlayers().get(1).getIdentity());
        assertEquals(AccessLevel.OPEN_GUI, loaded.getTrustedPlayers().get(1).getAccessLevel());
    }

    /** The keys are part of the mods' saves. */
    @Test
    void theSavedKeysStayTheSame() {
        CompoundTag tag = saved(manager);
        CompoundTag owner = NbtCompat.getCompound(tag, "Owner");
        assertEquals(OWNER.uuid(), NbtCompat.getUuid(owner, "UUID"));
        assertEquals("Owner", NbtCompat.getString(owner, "Name", ""));
        ListTag trusted = NbtCompat.getCompoundList(tag, "TrustedPlayers");
        assertEquals(2, trusted.size());
        CompoundTag alice = NbtCompat.getCompound(trusted, 0);
        assertEquals(ALICE.uuid(), NbtCompat.getUuid(alice, "UUID"));
        assertEquals("Alice", NbtCompat.getString(alice, "Name", ""));
        assertEquals("admin", NbtCompat.getString(alice, "AccessLevel", ""));
    }

    @Test
    void aBlockWithoutOwnerSavesNoOwnerAndLoadsAsUnowned() {
        manager.clearOwner();
        CompoundTag tag = saved(manager);
        assertFalse(NbtCompat.hasCompound(tag, "Owner"));
        assertTrue(NbtCompat.hasList(tag, "TrustedPlayers"));

        TrustManager loaded = newManager();
        loaded.setOwner(ALICE);
        SecurityNbt.load(loaded, tag);
        assertNull(loaded.getOwner());
        assertTrue(loaded.getTrustedPlayers().isEmpty());
    }

    @Test
    void loadingReplacesTheWholeState() {
        TrustManager loaded = newManager();
        loaded.load(BOB, List.of(new TrustedPlayer(OWNER, AccessLevel.ADMIN)));
        SecurityNbt.load(loaded, saved(manager));
        assertEquals(OWNER, loaded.getOwner());
        assertEquals(List.of(ALICE, BOB), loaded.getTrustedPlayers().stream().map(TrustedPlayer::getIdentity).toList());
    }

    @Test
    void anEmptyTagLoadsAsUnownedWithoutTrustList() {
        SecurityNbt.load(manager, new CompoundTag());
        assertNull(manager.getOwner());
        assertTrue(manager.getTrustedPlayers().isEmpty());
    }

    @Test
    void entriesWithoutUuidAreDroppedAndUnknownLevelsReadAsNone() {
        ListTag list = SecurityNbt.saveTrustedPlayers(List.of(new TrustedPlayer(ALICE, AccessLevel.ADMIN)));
        CompoundTag noUuid = new CompoundTag();
        noUuid.putString("Name", "Nobody");
        noUuid.putString("AccessLevel", "admin");
        list.add(noUuid);
        NbtCompat.getCompound(list, 0).putString("AccessLevel", "superuser");

        List<TrustedPlayer> loaded = SecurityNbt.loadTrustedPlayers(list);
        assertEquals(1, loaded.size());
        assertEquals(ALICE, loaded.get(0).getIdentity());
        assertEquals(AccessLevel.NONE, loaded.get(0).getAccessLevel());
    }

    @Test
    void aMissingNameReadsAsEmpty() {
        CompoundTag entry = new CompoundTag();
        NbtCompat.putUuid(entry, "UUID", ALICE.uuid());
        ListTag list = new ListTag();
        list.add(entry);
        List<TrustedPlayer> loaded = SecurityNbt.loadTrustedPlayers(list);
        assertEquals(new PlayerIdentity(ALICE.uuid(), ""), loaded.get(0).getIdentity());
        assertEquals(AccessLevel.NONE, loaded.get(0).getAccessLevel());
    }
}
