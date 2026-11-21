package omtteam.omlib.platform.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.compat.network.PacketType;
import omtteam.omlib.platform.Network;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public final class FabricNetwork implements Network {
    @Override
    public <T> void registerC2S(PacketType<T> type, NetworkCompat.ServerHandler<T> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, listener, buf, sender) -> {
            T packet = type.decode(buf); // decode before the buffer is released
            server.execute(() -> handler.handle(packet, player));
        });
    }

    @Override
    public <T> void registerS2C(PacketType<T> type) {
    }

    @Override
    public boolean canSend(ServerPlayer player, PacketType<?> type) {
        return ServerPlayNetworking.canSend(player, type.id());
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        type.encode(buf, packet);
        ServerPlayNetworking.send(player, type.id(), buf);
    }
}
