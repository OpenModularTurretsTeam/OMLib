package omtteam.omlib.api.tile;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.api.permission.EnumAccessLevel;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.player.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrustedPlayersManagerTileTest {
    private final TrustedPlayersManagerTile tpm = new TrustedPlayersManagerTile(null);

    @Test
    void testTrustedPlayerAdditionAndRemoval() {
        Player player = new Player(UUID.randomUUID(), "test", "test");
        ConfigHandler.offlineModeSupport = true;
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.getName()), "trustedPlayer needs to be gettable by name");
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.getUuid()), "trustedPlayer needs to be gettable by uuid");
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player), "trustedPlayer needs to be gettable by player");
            tpm.getTrustedPlayers().clear();
        }
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            tpm.removeTrustedPlayer(player.getName());
            assertNull(tpm.getTrustedPlayer(player), "removed trustedPlayer needs to be null");
            tpm.getTrustedPlayers().clear();
        }
        ConfigHandler.offlineModeSupport = false;
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.getName()), "trustedPlayer needs to be gettable by name");
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player), "trustedPlayer needs to be gettable by player");
            tpm.getTrustedPlayers().clear();
        }
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level);
            tpm.addTrustedPlayer(trustedPlayer);
            tpm.removeTrustedPlayer(player.getName());
            assertNull(tpm.getTrustedPlayer(player), "removed trustedPlayer needs to be null");
            tpm.getTrustedPlayers().clear();
        }
    }

    @Test
    void checkNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tpm.writeToNBT(tag);
        TrustedPlayersManagerTile that = new TrustedPlayersManagerTile(null);
        that.readFromNBT(tag);
        assertEquals(tpm.trustedPlayers, that.trustedPlayers);
    }
}
