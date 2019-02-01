package omtteam.omlib.util.player

import net.minecraft.nbt.NBTTagCompound
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class PlayerTest {
    Player player = new Player(UUID.randomUUID(), "test", "test")

    @Test
    void checkByteBuf() {

    }

    @Test
    void checkNBT() {
        NBTTagCompound tag = new NBTTagCompound()
        player.writeToNBT(tag)
        assertEquals(player, Player.readFromNBT(tag))
    }
}