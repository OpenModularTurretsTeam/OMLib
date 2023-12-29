package com.ommods.omlib.util;

import com.ommods.omlib.config.OMConfig;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OMPlayerTest {
    private final UUID uuid = UUID.randomUUID();
    private final String name = "test";
    private final String team = "test";
    private final OMPlayer player = new OMPlayer(uuid, name, team);

    @Test
    void checkByteBuf() {
        OMConfig.Permission.offlineModeSupport = true;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        OMPlayer.writeToByteBuf(player, buf);
        assertEquals(player, OMPlayer.readFromByteBuf(buf));
        OMConfig.Permission.offlineModeSupport = false;
        buf = new FriendlyByteBuf(Unpooled.buffer());
        OMPlayer.writeToByteBuf(player, buf);
        assertEquals(player, OMPlayer.readFromByteBuf(buf));
    }

    @Test
    void checkNBT() {
        OMConfig.Permission.offlineModeSupport = true;
        CompoundTag tag = player.serializeNBT();
        assertEquals(player, new OMPlayer(tag));
        OMConfig.Permission.offlineModeSupport = false;
        tag = player.serializeNBT();
        assertEquals(player, new OMPlayer(tag));
    }


    @Test
    void getUuid() {
        assertEquals(uuid, player.getUuid());
    }

    @Test
    void getName() {
        assertEquals(name, player.getName());
    }

    @Test
    void getTeam() {
        assertEquals(team, player.getTeam());
    }
}