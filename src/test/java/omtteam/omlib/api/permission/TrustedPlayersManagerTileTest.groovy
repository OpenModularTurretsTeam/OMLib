package omtteam.omlib.api.permission

import net.minecraft.nbt.NBTTagCompound
import omtteam.omlib.handler.ConfigHandler
import omtteam.omlib.util.player.Player
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class TrustedPlayersManagerTileTest {
    TrustedPlayersManagerTile tpm = new TrustedPlayersManagerTile()

    @Test
    void testTrustedPlayerAdditionAndRemoval() {
        Player player = new Player(UUID.randomUUID(), "test", "test")
        ConfigHandler.offlineModeSupport = true
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level)
            tpm.addTrustedPlayer(trustedPlayer)
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.name), "trustedPlayer needs to be gettable by name")
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.uuid), "trustedPlayer needs to be gettable by uuid")
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player), "trustedPlayer needs to be gettable by player")
            tpm.getTrustedPlayers().clear()
        }
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level)
            tpm.addTrustedPlayer(trustedPlayer)
            tpm.removeTrustedPlayer(player.getName())
            assertEquals(null, tpm.getTrustedPlayer(player), "removed trustedPlayer needs to be null")
            tpm.getTrustedPlayers().clear()
        }
        ConfigHandler.offlineModeSupport = false
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level)
            tpm.addTrustedPlayer(trustedPlayer)
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player.name), "trustedPlayer needs to be gettable by name")
            assertEquals(trustedPlayer, tpm.getTrustedPlayer(player), "trustedPlayer needs to be gettable by player")
            tpm.getTrustedPlayers().clear()
        }
        for (EnumAccessLevel level : EnumAccessLevel.values()) {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player, level)
            tpm.addTrustedPlayer(trustedPlayer)
            tpm.removeTrustedPlayer(player.getName())
            assertEquals(null, tpm.getTrustedPlayer(player), "removed trustedPlayer needs to be null")
            tpm.getTrustedPlayers().clear()
        }
    }

    @Test
    void checkNBT() {
        NBTTagCompound tag = new NBTTagCompound()
        tpm.writeToNBT(tag)
        TrustedPlayersManagerTile that = new TrustedPlayersManagerTile()
        that.readFromNBT(tag)
        assertEquals(tpm.trustedPlayers, that.trustedPlayers)
    }
}
