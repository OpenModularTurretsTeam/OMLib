package omtteam.omlib.platform.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.compat.network.PacketType;
import omtteam.omlib.platform.Network;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class FabricNetwork implements Network {
    @Override
    public <T> void registerC2S(PacketType<T> type, NetworkCompat.ServerHandler<T> handler) {
        PayloadTypeRegistry.playC2S().register(type.payloadType(), type.codec());
        // Fabric runs payload handlers on the server thread on 1.20.5+
        ServerPlayNetworking.registerGlobalReceiver(type.payloadType(),
                (payload, context) -> handler.handle(payload.packet(), context.player()));
    }

    @Override
    public <T> void registerS2C(PacketType<T> type) {
        PayloadTypeRegistry.playS2C().register(type.payloadType(), type.codec());
    }

    @Override
    public boolean canSend(ServerPlayer player, PacketType<?> type) {
        return ServerPlayNetworking.canSend(player, type.payloadType());
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        ServerPlayNetworking.send(player, type.wrap(packet));
    }
}
