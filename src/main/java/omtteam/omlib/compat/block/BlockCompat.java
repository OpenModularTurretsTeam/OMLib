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

    /** Whether the state renders as a full opaque cube. */
    public static boolean isSolidRender(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolidRender(level, pos);
    }

    /** How much light the state blocks, 0 to 15. */
    public static int lightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getLightBlock(level, pos);
    }

    /**
     * The render shape of a block that only a block entity renderer draws ({@code ENTITYBLOCK_ANIMATED}).
     */
    public static RenderShape entityRenderShape() {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    /**
     * A handled interaction that swings the player's arm.
     */
    public static InteractionResult sidedSuccess(boolean clientSide) {
        return InteractionResult.sidedSuccess(clientSide);
    }
}
