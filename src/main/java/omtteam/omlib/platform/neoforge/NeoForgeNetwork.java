package omtteam.omlib.platform.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import omtteam.omlib.compat.network.NetworkCompat;
import omtteam.omlib.compat.network.PacketType;
import omtteam.omlib.platform.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * NeoForge only accepts payloads during {@link RegisterPayloadHandlersEvent}, so registrations are recorded and
 * applied when it fires. Every mod that registers packets (this library for its own, each mod that uses it) applies the
 * registrations of its namespace in the event of its own mod bus ({@link #applyRegistrations}). Server-to-client
 * payloads need their handler at that point too: they get a dispatcher that looks up the handler the client registers
 * later ({@code NeoForgeClientNetwork}), so main never touches client code.
 */
public final class NeoForgeNetwork implements Network {
    private static final Logger LOGGER = LoggerFactory.getLogger("omlib");
    /** Version of the channel sets; raise it when a packet's format changes incompatibly. */
    private static final String PROTOCOL_VERSION = "1";

    /** The registrations still to apply, by namespace of the packet id. */
    private final Map<String, List<Consumer<PayloadRegistrar>>> pending = new HashMap<>();
    private final Set<String> applied = new HashSet<>();
    private final Map<PacketType<?>, Consumer<?>> clientHandlers = new ConcurrentHashMap<>();

    @Override
    public <T> void registerC2S(PacketType<T> type, NetworkCompat.ServerHandler<T> handler) {
        record(type, registrar -> registrar.playToServer(type.payloadType(), type.codec(),
                (payload, context) -> handler.handle(payload.packet(), (ServerPlayer) context.player())));
    }

    @Override
    public <T> void registerS2C(PacketType<T> type) {
        record(type, registrar -> registrar.playToClient(type.payloadType(), type.codec(),
                (payload, context) -> dispatchToClient(type, payload.packet())));
    }

    @Override
    public boolean canSend(ServerPlayer player, PacketType<?> type) {
        return player.connection.hasChannel(type.payloadType());
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, PacketType<T> type, T packet) {
        PacketDistributor.sendToPlayer(player, type.wrap(packet));
    }

    /**
     * Sets the client handler of a server-to-client packet; called by {@code NeoForgeClientNetwork}.
     */
    public <T> void setClientHandler(PacketType<T> type, Consumer<T> handler) {
        clientHandlers.put(type, handler);
    }

    private void record(PacketType<?> type, Consumer<PayloadRegistrar> registration) {
        String namespace = type.id().getNamespace();
        if (applied.contains(namespace)) {
            throw new IllegalStateException("Packets of " + namespace + " must be registered before RegisterPayloadHandlersEvent");
        }
        pending.computeIfAbsent(namespace, key -> new ArrayList<>()).add(registration);
    }

    /**
     * Applies the packets of {@code namespace} (the mod id of the mod whose event this is): each mod calls it from its
     * own {@link RegisterPayloadHandlersEvent} listener.
     */
    public void applyRegistrations(RegisterPayloadHandlersEvent event, String namespace) {
        // optional: like on Fabric, a missing channel on the other side is no reason to refuse the connection
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).executesOn(HandlerThread.MAIN).optional();
        pending.getOrDefault(namespace, List.of()).forEach(registration -> registration.accept(registrar));
        pending.remove(namespace);
        applied.add(namespace);
    }

    @SuppressWarnings("unchecked")
    private <T> void dispatchToClient(PacketType<T> type, T packet) {
        Consumer<T> handler = (Consumer<T>) clientHandlers.get(type);
        if (handler == null) {
            LOGGER.warn("No client handler for packet {}, dropped", type.id());
            return;
        }
        handler.accept(packet);
    }
}
