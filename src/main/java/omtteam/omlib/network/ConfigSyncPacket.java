package omtteam.omlib.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * S2C: the server's config of one mod as JSON, sent on join (one packet per mod that has a config manager).
 */
public record ConfigSyncPacket(String modId, String json) {
    private static final int MAX_LENGTH = 262144;

    public static void write(FriendlyByteBuf buf, ConfigSyncPacket packet) {
        buf.writeUtf(packet.modId, 64);
        buf.writeUtf(packet.json, MAX_LENGTH);
    }

    public static ConfigSyncPacket read(FriendlyByteBuf buf) {
        return new ConfigSyncPacket(buf.readUtf(64), buf.readUtf(MAX_LENGTH));
    }
}
