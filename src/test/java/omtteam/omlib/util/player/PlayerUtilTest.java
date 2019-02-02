package omtteam.omlib.util.player;

import omtteam.omlib.api.permission.EnumAccessLevel;
import omtteam.omlib.api.permission.EnumPlayerAccessType;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.api.tile.TrustedPlayersManagerTile;
import omtteam.omlib.handler.OMConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerUtilTest {
    private Player playerNone;
    private Player playerUntrusted;
    private Player playerAdmin;
    private Player playerChangeSettings;
    private Player playerCanOpenGUI;
    private Player owner;
    private OwnedBlock ownedBlock;
    private TrustedPlayer trustedPlayerNone;
    private TrustedPlayer trustedPlayerAdmin;
    private TrustedPlayer trustedPlayerChangeSettings;
    private TrustedPlayer trustedPlayerOpenGUI;

    @BeforeEach
    void setUp() {
        OMConfig.GENERAL.offlineModeSupport = false;
        playerNone = new Player(UUID.randomUUID(), "playerNone", "test");
        playerUntrusted = new Player(UUID.randomUUID(), "playerUntrusted", "test");
        playerAdmin = new Player(UUID.randomUUID(), "playerAdmin", "test");
        playerChangeSettings = new Player(UUID.randomUUID(), "playerChangeSettings", "test");
        playerCanOpenGUI = new Player(UUID.randomUUID(), "playerCanOpenGUI", "test");
        owner = new Player(UUID.randomUUID(), "owner", "test");
        ownedBlock = new OwnedBlock(owner);
        trustedPlayerNone = new TrustedPlayer(playerNone);
        trustedPlayerAdmin = new TrustedPlayer(playerAdmin);
        trustedPlayerChangeSettings = new TrustedPlayer(playerChangeSettings);
        trustedPlayerOpenGUI = new TrustedPlayer(playerCanOpenGUI);
        trustedPlayerAdmin.setAccessLevel(EnumAccessLevel.ADMIN);
        trustedPlayerChangeSettings.setAccessLevel(EnumAccessLevel.CHANGE_SETTINGS);
        trustedPlayerOpenGUI.setAccessLevel(EnumAccessLevel.OPEN_GUI);
        trustedPlayerNone.setAccessLevel(EnumAccessLevel.NONE);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerNone);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerAdmin);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerOpenGUI);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerChangeSettings);
    }

    void setOfflineMode() {
        OMConfig.GENERAL.offlineModeSupport = true;
        playerNone = new Player(null, "playerNone", "test");
        playerUntrusted = new Player(null, "playerUntrusted", "test");
        playerAdmin = new Player(null, "playerAdmin", "test");
        playerChangeSettings = new Player(null, "playerChangeSettings", "test");
        playerCanOpenGUI = new Player(null, "playerCanOpenGUI", "test");
        owner = new Player(null, "owner", "test");
        ownedBlock = new OwnedBlock(owner);
        trustedPlayerNone = new TrustedPlayer(playerNone);
        trustedPlayerAdmin = new TrustedPlayer(playerAdmin);
        trustedPlayerChangeSettings = new TrustedPlayer(playerChangeSettings);
        trustedPlayerOpenGUI = new TrustedPlayer(playerCanOpenGUI);
        trustedPlayerAdmin.setAccessLevel(EnumAccessLevel.ADMIN);
        trustedPlayerChangeSettings.setAccessLevel(EnumAccessLevel.CHANGE_SETTINGS);
        trustedPlayerOpenGUI.setAccessLevel(EnumAccessLevel.OPEN_GUI);
        trustedPlayerNone.setAccessLevel(EnumAccessLevel.NONE);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerNone);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerAdmin);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerOpenGUI);
        ownedBlock.getTrustManager().addTrustedPlayer(trustedPlayerChangeSettings);
    }

    @Test
    void getTrustedPlayer() {
        assertEquals(trustedPlayerNone, PlayerUtil.getTrustedPlayer(playerNone, ownedBlock));
        setOfflineMode();
        assertEquals(trustedPlayerNone, PlayerUtil.getTrustedPlayer(playerNone, ownedBlock));
    }

    @Test
    void getPlayerType() {
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock));
        setOfflineMode();
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock));
    }

    @Test
    void isPlayerTrusted() {
        assertTrue(PlayerUtil.isPlayerTrusted(playerNone, ownedBlock));
        assertFalse(PlayerUtil.isPlayerTrusted(playerUntrusted, ownedBlock));
        setOfflineMode();
        assertTrue(PlayerUtil.isPlayerTrusted(playerNone, ownedBlock));
        assertFalse(PlayerUtil.isPlayerTrusted(playerUntrusted, ownedBlock));
    }

    @Test
    void isPlayerOwner() {
        assertTrue(PlayerUtil.isPlayerOwner(owner, ownedBlock));
        assertFalse(PlayerUtil.isPlayerOwner(playerNone, ownedBlock));
        assertFalse(PlayerUtil.isPlayerOwner(playerUntrusted, ownedBlock));
        setOfflineMode();
        assertTrue(PlayerUtil.isPlayerOwner(owner, ownedBlock));
        assertFalse(PlayerUtil.isPlayerOwner(playerNone, ownedBlock));
        assertFalse(PlayerUtil.isPlayerOwner(playerUntrusted, ownedBlock));
    }

    @Test
    void isPlayerAdmin() {
        assertTrue(PlayerUtil.isPlayerAdmin(owner, ownedBlock));
        assertTrue(PlayerUtil.isPlayerAdmin(playerAdmin, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerChangeSettings, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerCanOpenGUI, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerNone, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerUntrusted, ownedBlock));
        setOfflineMode();
        assertTrue(PlayerUtil.isPlayerAdmin(owner, ownedBlock));
        assertTrue(PlayerUtil.isPlayerAdmin(playerAdmin, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerChangeSettings, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerCanOpenGUI, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerNone, ownedBlock));
        assertFalse(PlayerUtil.isPlayerAdmin(playerUntrusted, ownedBlock));
    }

    @Test
    void canPlayerChangeSetting() {
        assertTrue(PlayerUtil.canPlayerChangeSetting(owner, ownedBlock));
        assertTrue(PlayerUtil.canPlayerChangeSetting(playerAdmin, ownedBlock));
        assertTrue(PlayerUtil.canPlayerChangeSetting(playerChangeSettings, ownedBlock));
        assertFalse(PlayerUtil.canPlayerChangeSetting(playerCanOpenGUI, ownedBlock));
        assertFalse(PlayerUtil.canPlayerChangeSetting(playerNone, ownedBlock));
        assertFalse(PlayerUtil.canPlayerChangeSetting(playerUntrusted, ownedBlock));
        setOfflineMode();
        assertTrue(PlayerUtil.canPlayerChangeSetting(owner, ownedBlock));
        assertTrue(PlayerUtil.canPlayerChangeSetting(playerAdmin, ownedBlock));
        assertTrue(PlayerUtil.canPlayerChangeSetting(playerChangeSettings, ownedBlock));
        assertFalse(PlayerUtil.canPlayerChangeSetting(playerCanOpenGUI, ownedBlock));
        assertFalse(PlayerUtil.canPlayerChangeSetting(playerNone, ownedBlock));
        assertFalse(PlayerUtil.canPlayerChangeSetting(playerUntrusted, ownedBlock));
    }

    @Test
    void canPlayerAccessBlock() {
        assertTrue(PlayerUtil.canPlayerAccessBlock(owner, ownedBlock));
        assertTrue(PlayerUtil.canPlayerAccessBlock(playerAdmin, ownedBlock));
        assertTrue(PlayerUtil.canPlayerAccessBlock(playerChangeSettings, ownedBlock));
        assertTrue(PlayerUtil.canPlayerAccessBlock(playerCanOpenGUI, ownedBlock));
        assertFalse(PlayerUtil.canPlayerAccessBlock(playerNone, ownedBlock));
        assertFalse(PlayerUtil.canPlayerAccessBlock(playerUntrusted, ownedBlock));
        setOfflineMode();
        assertTrue(PlayerUtil.canPlayerAccessBlock(owner, ownedBlock));
        assertTrue(PlayerUtil.canPlayerAccessBlock(playerAdmin, ownedBlock));
        assertTrue(PlayerUtil.canPlayerAccessBlock(playerChangeSettings, ownedBlock));
        assertTrue(PlayerUtil.canPlayerAccessBlock(playerCanOpenGUI, ownedBlock));
        assertFalse(PlayerUtil.canPlayerAccessBlock(playerNone, ownedBlock));
        assertFalse(PlayerUtil.canPlayerAccessBlock(playerUntrusted, ownedBlock));
    }

    @Test
    void getPlayerAccessType() {
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerAdmin, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerChangeSettings, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerCanOpenGUI, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerNone, ownedBlock));
        assertEquals(EnumPlayerAccessType.NONE, PlayerUtil.getPlayerAccessType(playerUntrusted, ownedBlock));
        setOfflineMode();
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerAdmin, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerChangeSettings, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerCanOpenGUI, ownedBlock));
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerNone, ownedBlock));
        assertEquals(EnumPlayerAccessType.NONE, PlayerUtil.getPlayerAccessType(playerUntrusted, ownedBlock));
    }

    class OwnedBlock implements IHasOwner, IHasTrustManager {
        final Player owner;
        final TrustedPlayersManagerTile tile = new TrustedPlayersManagerTile(null);

        OwnedBlock(Player owner) {
            this.owner = owner;
        }

        @Override
        public TrustedPlayersManagerTile getTrustManager() {
            return tile;
        }

        @Override
        public String getOwner() {
            if (owner.getUuid() != null) {
                return owner.getUuid().toString();
            }
            return "";
        }

        @Override
        public String getOwnerName() {
            return owner.getName();
        }

        @Override
        public String getOwnerTeamName() {
            return owner.getTeamName();
        }
    }
}