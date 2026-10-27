package omtteam.omlib.network;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Write and read of the library's packets, as the loaders' codecs call them. */
class PacketCodecTest {
    private static final BlockPos POS = new BlockPos(-120, -60, 3_000_000);
    private static final UUID PLAYER = UUID.randomUUID();

    private static <T> T roundTrip(T packet, BiConsumer<FriendlyByteBuf, T> write, Function<FriendlyByteBuf, T> read) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        write.accept(buf, packet);
        T result = read.apply(buf);
        assertEquals(0, buf.readableBytes(), "everything written is read");
        return result;
    }

    @Test
    void trustPacketsSurviveTheRoundTrip() {
        TrustPackets.Add add = new TrustPackets.Add(POS, "Alice_1234567890");
        assertEquals(add, roundTrip(add, TrustPackets.Add::write, TrustPackets.Add::read));
        TrustPackets.Remove remove = new TrustPackets.Remove(POS, PLAYER);
        assertEquals(remove, roundTrip(remove, TrustPackets.Remove::write, TrustPackets.Remove::read));
        TrustPackets.Disown disown = new TrustPackets.Disown(POS);
        assertEquals(disown, roundTrip(disown, TrustPackets.Disown::write, TrustPackets.Disown::read));
    }

    @Test
    void accessChangesKeepTheirSign() {
        for (int delta : new int[]{1, -1}) {
            TrustPackets.ChangeAccess packet = new TrustPackets.ChangeAccess(POS, PLAYER, delta);
            assertEquals(packet, roundTrip(packet, TrustPackets.ChangeAccess::write, TrustPackets.ChangeAccess::read));
        }
    }

    @Test
    void namesLongerThanAUsernameAreRejected() {
        String name = "a".repeat(TrustPackets.MAX_NAME_LENGTH + 1);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        assertThrows(RuntimeException.class, () -> TrustPackets.Add.write(buf, new TrustPackets.Add(POS, name)));

        // a modified client could still send one
        FriendlyByteBuf sent = new FriendlyByteBuf(Unpooled.buffer());
        sent.writeBlockPos(POS);
        sent.writeUtf(name);
        assertThrows(RuntimeException.class, () -> TrustPackets.Add.read(sent));
    }

    @Test
    void camoPacketsSurviveTheRoundTrip() {
        CamoPackets.OpenMenu open = new CamoPackets.OpenMenu(POS);
        assertEquals(open, roundTrip(open, CamoPackets.OpenMenu::write, CamoPackets.OpenMenu::read));
        CamoPackets.Back back = new CamoPackets.Back(POS);
        assertEquals(back, roundTrip(back, CamoPackets.Back::write, CamoPackets.Back::read));
        CamoPackets.SetLight light = new CamoPackets.SetLight(POS, 15, 0);
        assertEquals(light, roundTrip(light, CamoPackets.SetLight::write, CamoPackets.SetLight::read));
    }

    /** The handler clamps the values ({@code CamoLight}), so the codec passes out-of-range ones through. */
    @Test
    void lightValuesAreNotLimitedByTheCodec() {
        CamoPackets.SetLight light = new CamoPackets.SetLight(POS, -1, 999);
        assertEquals(light, roundTrip(light, CamoPackets.SetLight::write, CamoPackets.SetLight::read));
    }

    @Test
    void configsLargerThanVanillasStringLimitAreSent() {
        ConfigSyncPacket packet = new ConfigSyncPacket("openmodularturrets", "{\"value\":\"" + "x".repeat(100_000) + "\"}");
        assertEquals(packet, roundTrip(packet, ConfigSyncPacket::write, ConfigSyncPacket::read));
    }

    @Test
    void configsAboveTheLimitAreRejected() {
        ConfigSyncPacket packet = new ConfigSyncPacket("mod", "x".repeat(262_145));
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        assertThrows(RuntimeException.class, () -> ConfigSyncPacket.write(buf, packet));
    }
}
