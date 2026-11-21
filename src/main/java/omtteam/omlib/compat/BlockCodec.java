package omtteam.omlib.compat;

import net.minecraft.world.level.block.BaseEntityBlock;
import com.mojang.serialization.MapCodec;

/**
 * Handle on the codec of a block, the same on every version branch.
 * The game needs one per block (it registers the block type, see {@code BaseEntityBlock#codec()}).
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
