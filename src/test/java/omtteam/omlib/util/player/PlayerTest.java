package omtteam.omlib.util.player;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTest {
    private final Player player = new Player(UUID.randomUUID(), "test", "test");

    @Test
    void checkByteBuf() {
        ByteBuf buf = Unpooled.buffer();
        Player.writeToByteBuf(player, buf);
        assertEquals(player, Player.readFromByteBuf(buf));
    }

    @Test
    void checkNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        player.writeToNBT(tag);
        assertEquals(player, Player.readFromNBT(tag));
    }
}