package omtteam.omlib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implemented by blocks that take part in claiming without an owner of their own: a block that belongs to another block
 * (a turret to its base) and a block that joins the owned blocks around it into one claim. Blocks that have an owner
 * ({@code OwnedHost} block entities) need no link.
 */
public interface ClaimLink {
    /** The block whose owner counts when this block is clicked; the block itself by default. */
    default BlockPos ownerPos(BlockState state, BlockPos pos) {
        return pos;
    }

    /** Whether claims and the ownership wand spread through this block to the owned blocks next to it. */
    default boolean connectsClaims() {
        return false;
    }
}
