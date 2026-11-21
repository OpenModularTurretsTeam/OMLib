package omtteam.omlib.client.platform.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import omtteam.omlib.client.platform.ClientNetwork;
import omtteam.omlib.compat.network.PacketType;

import java.util.function.Consumer;

public final class FabricClientNetwork implements ClientNetwork {
    @Override
    public <T> void registerHandler(PacketType<T> type, Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type.payloadType(), (payload, context) -> handler.accept(payload.packet()));
    }

    @Override
    public <T> void sendToServer(PacketType<T> type, T packet) {
        ClientPlayNetworking.send(type.wrap(packet));
    }
}
