package omtteam.omlib.client.compat;

import omtteam.omlib.client.platform.ClientPlatform;
import omtteam.omlib.compat.network.PacketType;

import java.util.function.Consumer;

/**
 * Client-side counterpart of {@link omtteam.omlib.compat.network.NetworkCompat}; the transport is
 * {@link omtteam.omlib.client.platform.ClientNetwork}.
 */
public final class ClientNetworkCompat {
    private ClientNetworkCompat() {
    }

    /**
     * Registers the client handler of a server-to-client packet. The handler runs on the client thread.
     */
    public static <T> void registerHandler(PacketType<T> type, Consumer<T> handler) {
        ClientPlatform.network().registerHandler(type, handler);
    }

    public static <T> void sendToServer(PacketType<T> type, T packet) {
        ClientPlatform.network().sendToServer(type, packet);
    }
}
