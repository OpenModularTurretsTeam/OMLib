package omtteam.omlib.compat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A registered packet type. Packets are plain objects (usually records) with an encoder and a decoder;
 * the transport (a raw channel on this version) is hidden here.
 * Create instances through {@link NetworkCompat}.
 */
public final class PacketType<T> {
    private final ResourceLocation id;
    private final BiConsumer<FriendlyByteBuf, T> encoder;
    private final Function<FriendlyByteBuf, T> decoder;

    PacketType(ResourceLocation id, BiConsumer<FriendlyByteBuf, T> encoder, Function<FriendlyByteBuf, T> decoder) {
        this.id = id;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public ResourceLocation id() {
        return id;
    }

    @SuppressWarnings("unused")
    public void encode(FriendlyByteBuf buf, T packet) {
        encoder.accept(buf, packet);
    }

    @SuppressWarnings("unused")
    public T decode(FriendlyByteBuf buf) {
        return decoder.apply(buf);
    }

}
