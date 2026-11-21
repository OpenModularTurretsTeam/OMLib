package omtteam.omlib.client.platform.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import omtteam.omlib.client.platform.ClientNetwork;
import omtteam.omlib.compat.network.PacketType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public final class FabricClientNetwork implements ClientNetwork {
    @Override
    public <T> void registerHandler(PacketType<T> type, Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, listener, buf, sender) -> {
            T packet = type.decode(buf); // decode before the buffer is released
            client.execute(() -> handler.accept(packet));
        });
    }

    @Override
    public <T> void sendToServer(PacketType<T> type, T packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        type.encode(buf, packet);
        ClientPlayNetworking.send(type.id(), buf);
    }
}
