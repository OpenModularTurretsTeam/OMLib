package omtteam.omlib.util.player;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.config.OMConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PlayerTest {
    private final Player player = new Player(UUID.randomUUID(), "test", "test");

    @AfterEach
    void tearDown() {
        OMConfig.GENERAL.offlineModeSupport = false;
    }

    @Test
    void aRenamedPlayerIsTheSamePlayer() {
        UUID uuid = UUID.randomUUID();
        Player before = new Player(uuid, "OldName");
        Player after = new Player(uuid, "NewName");
        assertEquals(before, after);
        assertEquals(before.hashCode(), after.hashCode());
        assertNotEquals(before, new Player(UUID.randomUUID(), "OldName"));
    }

    @Test
    void offlineModeComparesNamesInAnyCase() {
        OMConfig.GENERAL.offlineModeSupport = true;
        Player a = new Player(UUID.randomUUID(), "Steve");
        Player b = new Player(UUID.randomUUID(), "steve");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Player(a.getUuid(), "Alex"));
    }

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