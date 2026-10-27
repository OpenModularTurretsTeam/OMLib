package omtteam.omlib.compat.network;

import io.netty.buffer.Unpooled;
//? if >=1.21 {
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
//?}
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import omtteam.omlib.compat.VersionCompat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/** The packet type's own encoder and decoder, and on 1.21+ the payload codec the loaders register. */
class PacketTypeTest {
    private record Ping(int value, String text) {
        static void write(FriendlyByteBuf buf, Ping ping) {
            buf.writeVarInt(ping.value);
            buf.writeUtf(ping.text);
        }

        static Ping read(FriendlyByteBuf buf) {
            return new Ping(buf.readVarInt(), buf.readUtf());
        }
    }

    private static final ResourceLocation ID = VersionCompat.id("omlib", "ping");
    private static final Ping PING = new Ping(-300, "pong");

    private static PacketType<Ping> type() {
        return new PacketType<>(ID, Ping::write, Ping::read);
    }

    @Test
    void encodesAndDecodesWithItsCodec() {
        PacketType<Ping> type = type();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        type.encode(buf, PING);
        assertEquals(PING, type.decode(buf));
        assertEquals(0, buf.readableBytes(), "everything written is read");
        assertEquals(ID, type.id());
    }

    //? if >=1.21 {
    @Test
    void payloadsCarryThePacketThroughTheStreamCodec() {
        PacketType<Ping> type = type();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        type.codec().encode(buf, type.wrap(PING));
        PacketType.Payload<Ping> payload = type.codec().decode(buf);
        assertEquals(0, buf.readableBytes(), "everything written is read");
        assertEquals(PING, payload.packet());
        assertSame(type, payload.packetType());
        assertSame(type.payloadType(), payload.type());
        assertEquals(ID, type.payloadType().id());
    }
    //?}
}
