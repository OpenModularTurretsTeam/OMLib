package omtteam.omlib.compat;

import net.minecraft.world.level.block.BaseEntityBlock;

/**
 * Handle on the codec of a block, the same on every version branch.
 * This version has no block codec, so the handle is empty and only keeps the call sites the same.
 * Create it with {@link CompatEntityBlock}'s {@code codecOf} helpers and pass it to its constructor.
 */
public final class BlockCodec<T extends BaseEntityBlock> {
    BlockCodec() {
    }
}
