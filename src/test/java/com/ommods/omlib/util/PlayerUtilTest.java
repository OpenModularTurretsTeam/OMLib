package com.ommods.omlib.util;

import com.ommods.omlib.block.OMBlockEntity;
import com.ommods.omlib.config.OMConfig;
import com.ommods.omlib.util.permission.*;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerUtilTest {
    private OMPlayer playerNone;
    private OMPlayer playerUntrusted;
    private OMPlayer playerAdmin;
    private OMPlayer playerChangeSettings;
    private OMPlayer playerCanOpenGUI;
    private OMPlayer owner;
    private OwnedBlock ownedBlock;
    private TrustedPlayer trustedPlayerNone;
    private TrustedPlayer trustedPlayerAdmin;
    private TrustedPlayer trustedPlayerChangeSettings;
    private TrustedPlayer trustedPlayerOpenGUI;

    @BeforeEach
    void setUp() {
        OMConfig.Permission.offlineModeSupport = false;
        playerNone = new OMPlayer(UUID.randomUUID(), "playerNone", "test");
        playerUntrusted = new OMPlayer(UUID.randomUUID(), "playerUntrusted", "test");
        playerAdmin = new OMPlayer(UUID.randomUUID(), "playerAdmin", "test");
        playerChangeSettings = new OMPlayer(UUID.randomUUID(), "playerChangeSettings", "test");
        playerCanOpenGUI = new OMPlayer(UUID.randomUUID(), "playerCanOpenGUI", "test");
        owner = new OMPlayer(UUID.randomUUID(), "owner", "test");
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
        OMConfig.Permission.offlineModeSupport = true;
        playerNone = new OMPlayer(null, "playerNone", "test");
        playerUntrusted = new OMPlayer(null, "playerUntrusted", "test");
        playerAdmin = new OMPlayer(null, "playerAdmin", "test");
        playerChangeSettings = new OMPlayer(null, "playerChangeSettings", "test");
        playerCanOpenGUI = new OMPlayer(null, "playerCanOpenGUI", "test");
        owner = new OMPlayer(null, "owner", "test");
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

    static class OwnedBlock extends OMBlockEntity implements IHasTrustManager {
        final OMPlayer owner;
        final TrustManager tile = new TrustManager();

        OwnedBlock(OMPlayer owner) {
            super(null, BlockPos.ZERO, null);
            this.owner = owner;
        }

        @Override
        public TrustManager getTrustManager() {
            return tile;
        }

        @Override
        public OMPlayer getOwner() {
            return owner;
        }
    }
}