package omtteam.omlib.compat.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block and block state calls behind one set of signatures, so that call sites are the same on every version branch.
 * <p>
 * Depends on Minecraft only (like {@code compat.nbt.NbtCompat}), so packages outside the package cycle around
 * {@code compat} (e.g. {@code camo}) may use it.
 */
public final class BlockCompat {
    private BlockCompat() {
    }

    /** Whether the state renders as a full opaque cube (the game caches it per state, so the position is not used). */
    public static boolean isSolidRender(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolidRender();
    }

    /** How much light the state blocks, 0 to 15 (the game caches it per state, so the position is not used). */
    public static int lightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getLightBlock();
    }

    /**
     * The render shape of a block that only a block entity renderer draws (the game has no {@code ENTITYBLOCK_ANIMATED}
     * any more; its renderers draw whatever the shape is, so the block is invisible).
     */
    public static RenderShape entityRenderShape() {
        return RenderShape.INVISIBLE;
    }

    /**
     * A handled interaction that swings the player's arm ({@code InteractionResult.SUCCESS} swings on the client and is
     * the same result on both sides).
     */
    public static InteractionResult sidedSuccess(boolean clientSide) {
        return InteractionResult.SUCCESS;
    }
}
