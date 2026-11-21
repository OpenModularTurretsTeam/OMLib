package omtteam.omlib.compat;

import net.minecraft.world.level.block.BaseEntityBlock;
import com.mojang.serialization.MapCodec;

/**
 * Version-independent handle on the codec of a block.
 * 1.21 needs one per block (it registers the block type, see {@code BaseEntityBlock#codec()}), 1.20.1 has none.
 * Create it with {@link CompatEntityBlock}'s {@code codecOf} helpers and pass it to its constructor.
 */
public final class BlockCodec<T extends BaseEntityBlock> {
    private final MapCodec<T> codec;

    BlockCodec(MapCodec<T> codec) {
        this.codec = codec;
    }

    MapCodec<T> codec() {
        return codec;
    }
}
