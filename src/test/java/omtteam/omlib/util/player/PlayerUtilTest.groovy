package omtteam.omlib.util.player

import omtteam.omlib.api.permission.*
import omtteam.omlib.handler.ConfigHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class PlayerUtilTest {
    Player playerNone = new Player(UUID.randomUUID(), "playerNone", "test")
    Player playerUntrusted = new Player(UUID.randomUUID(), "playerUntrusted", "test")
    Player playerAdmin = new Player(UUID.randomUUID(), "playerAdmin", "test")
    Player playerChangeSettings = new Player(UUID.randomUUID(), "playerChangeSettings", "test")
    Player playerCanOpenGUI = new Player(UUID.randomUUID(), "playerCanOpenGUI", "test")
    Player owner = new Player(UUID.randomUUID(), "owner", "test")
    OwnedBlock ownedBlock = new OwnedBlock(owner)

    class OwnedBlock implements IHasOwner, IHasTrustManager {
        Player owner
        TrustedPlayersManagerTile tile = new TrustedPlayersManagerTile()

        @Override
        TrustedPlayersManagerTile getTrustManager() {
            return tile
        }

        OwnedBlock(Player owner) {
            this.owner = owner
        }

        @Override
        String getOwner() {
            return owner.uuid.toString()
        }

        @Override
        String getOwnerName() {
            return owner.name
        }

        @Override
        String getOwnerTeamName() {
            return owner.teamName
        }
    }
    TrustedPlayer trustedPlayerNone = new TrustedPlayer(playerNone)
    TrustedPlayer trustedPlayerAdmin = new TrustedPlayer(playerAdmin)
    TrustedPlayer trustedPlayerChangeSettings = new TrustedPlayer(playerChangeSettings)
    TrustedPlayer trustedPlayerOpenGUI = new TrustedPlayer(playerCanOpenGUI)


    @BeforeEach
    void setUp() {
        trustedPlayerAdmin.accessLevel = EnumAccessLevel.ADMIN
        trustedPlayerChangeSettings.accessLevel = EnumAccessLevel.CHANGE_SETTINGS
        trustedPlayerOpenGUI.accessLevel = EnumAccessLevel.OPEN_GUI
        trustedPlayerNone.accessLevel = EnumAccessLevel.NONE
        ownedBlock.trustManager.addTrustedPlayer(trustedPlayerNone)
        ownedBlock.trustManager.addTrustedPlayer(trustedPlayerAdmin)
        ownedBlock.trustManager.addTrustedPlayer(trustedPlayerOpenGUI)
        ownedBlock.trustManager.addTrustedPlayer(trustedPlayerChangeSettings)
    }

    @AfterEach
    void tearDown() {
        ownedBlock.trustManager.trustedPlayers.clear()
    }

    @Test
    void getTrustedPlayer() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(trustedPlayerNone, PlayerUtil.getTrustedPlayer(playerNone, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(trustedPlayerNone, PlayerUtil.getTrustedPlayer(playerNone, ownedBlock))
    }

    @Test
    void getPlayerType() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock))
    }

    @Test
    void isPlayerTrusted() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(true, PlayerUtil.isPlayerTrusted(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerTrusted(playerUntrusted, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(true, PlayerUtil.isPlayerTrusted(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerTrusted(playerUntrusted, ownedBlock))
    }


    @Test
    void isPlayerOwner() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(true, PlayerUtil.isPlayerOwner(owner, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerOwner(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerOwner(playerUntrusted, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(true, PlayerUtil.isPlayerOwner(owner, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerOwner(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerOwner(playerUntrusted, ownedBlock))
    }

    @Test
    void isPlayerAdmin() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(true, PlayerUtil.isPlayerAdmin(owner, ownedBlock))
        assertEquals(true, PlayerUtil.isPlayerAdmin(playerAdmin, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerChangeSettings, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerCanOpenGUI, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerUntrusted, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(true, PlayerUtil.isPlayerAdmin(owner, ownedBlock))
        assertEquals(true, PlayerUtil.isPlayerAdmin(playerAdmin, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerChangeSettings, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerCanOpenGUI, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.isPlayerAdmin(playerUntrusted, ownedBlock))
    }


    @Test
    void canPlayerChangeSetting() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(true, PlayerUtil.canPlayerChangeSetting(owner, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerChangeSetting(playerAdmin, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerChangeSetting(playerChangeSettings, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerChangeSetting(playerCanOpenGUI, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerChangeSetting(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerChangeSetting(playerUntrusted, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(true, PlayerUtil.canPlayerChangeSetting(owner, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerChangeSetting(playerAdmin, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerChangeSetting(playerChangeSettings, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerChangeSetting(playerCanOpenGUI, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerChangeSetting(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerChangeSetting(playerUntrusted, ownedBlock))
    }

    @Test
    void canPlayerAccessBlock() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(owner, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(playerAdmin, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(playerChangeSettings, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(playerCanOpenGUI, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerAccessBlock(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerAccessBlock(playerUntrusted, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(owner, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(playerAdmin, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(playerChangeSettings, ownedBlock))
        assertEquals(true, PlayerUtil.canPlayerAccessBlock(playerCanOpenGUI, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerAccessBlock(playerNone, ownedBlock))
        assertEquals(false, PlayerUtil.canPlayerAccessBlock(playerUntrusted, ownedBlock))
    }

    @Test
    void getPlayerAccessType() {
        ConfigHandler.offlineModeSupport = true
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerAdmin, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerChangeSettings, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerCanOpenGUI, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerNone, ownedBlock))
        assertEquals(EnumPlayerAccessType.NONE, PlayerUtil.getPlayerAccessType(playerUntrusted, ownedBlock))
        ConfigHandler.offlineModeSupport = false
        assertEquals(EnumPlayerAccessType.OWNER, PlayerUtil.getPlayerAccessType(owner, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerAdmin, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerChangeSettings, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerCanOpenGUI, ownedBlock))
        assertEquals(EnumPlayerAccessType.TRUSTED, PlayerUtil.getPlayerAccessType(playerNone, ownedBlock))
        assertEquals(EnumPlayerAccessType.NONE, PlayerUtil.getPlayerAccessType(playerUntrusted, ownedBlock))
    }
}