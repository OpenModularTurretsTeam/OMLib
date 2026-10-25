package omtteam.omlib.handler;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.api.permission.IOwnedHost;
import omtteam.omlib.api.tile.IOwnedBlockAddon;
import omtteam.omlib.config.OMConfig;
import omtteam.omlib.core.world.BlockPosition;
import omtteam.omlib.core.world.FloodFill;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Owned blocks connected to each other through their faces, for claiming and the ownership wand (1.20.1+
 * {@code ClaimSpread}). Addons of an owned block ({@link IOwnedBlockAddon}: OMT's turrets and cranks) connect the
 * blocks around them without having an owner themselves. Only loaded blocks are walked.
 */
public final class ClaimSpread {
    private ClaimSpread() {
    }

    /** What a claim did: the number of blocks claimed, and whether more connected blocks were left past the limit. */
    public static final class Result {
        private final int claimed;
        private final boolean limitReached;

        public Result(int claimed, boolean limitReached) {
            this.claimed = claimed;
            this.limitReached = limitReached;
        }

        public int claimed() {
            return claimed;
        }

        public boolean limitReached() {
            return limitReached;
        }
    }

    /**
     * Owned blocks connected to {@code start} through faces (addons connect but are not returned) for which
     * {@code joins} is true, nearest first; walks at most {@code limit} blocks, addons included. Empty if the block at
     * {@code start} does not join.
     */
    public static List<IOwnedHost> connected(World world, BlockPos start, int limit, Predicate<IOwnedHost> joins) {
        return hosts(world, walk(world, start, limit, joins));
    }

    /**
     * Claims the unowned blocks connected to {@code start}, up to {@code claimSpreadLimit} walked blocks. Blocks that
     * have an owner are never taken, and the walk does not pass through them.
     */
    public static Result claim(World world, BlockPos start, Player player) {
        int limit = OMConfig.GENERAL.claimSpreadLimit;
        // one block more than the limit tells whether anything was left out
        List<BlockPosition> walked = walk(world, start, limit + 1, host -> host.getOwnerOrNull() == null);
        boolean limitReached = walked.size() > limit;
        int claimed = 0;
        for (IOwnedHost host : hosts(world, walked.subList(0, Math.min(limit, walked.size())))) {
            if (host.claim(player)) claimed++;
        }
        return new Result(claimed, limitReached);
    }

    /** The block whose owner counts for a click at {@code pos}: an addon's owned block, otherwise the block itself. */
    public static BlockPos ownerPos(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IOwnedBlockAddon) {
            // a turret without base has no linked block, although the interface promises one
            TileEntityOwnedBlock linked = ((IOwnedBlockAddon) tile).getLinkedBlock();
            //noinspection ConstantConditions
            if (linked != null) return linked.getPos();
        }
        return pos;
    }

    /** The owned block that counts for a click at {@code pos} ({@link #ownerPos}), if it is one. */
    @Nullable
    public static IOwnedHost ownedHost(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(ownerPos(world, pos));
        return tile instanceof IOwnedHost ? (IOwnedHost) tile : null;
    }

    private static List<BlockPosition> walk(World world, BlockPos start, int limit, Predicate<IOwnedHost> joins) {
        return FloodFill.collect(position(start), position -> joins(world, blockPos(position), joins), limit);
    }

    private static boolean joins(World world, BlockPos pos, Predicate<IOwnedHost> joins) {
        if (!world.isBlockLoaded(pos)) return false;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IOwnedBlockAddon) return true;
        return tile instanceof IOwnedHost && joins.test((IOwnedHost) tile);
    }

    private static List<IOwnedHost> hosts(World world, List<BlockPosition> positions) {
        List<IOwnedHost> hosts = new ArrayList<>();
        for (BlockPosition position : positions) {
            TileEntity tile = world.getTileEntity(blockPos(position));
            if (tile instanceof IOwnedHost) hosts.add((IOwnedHost) tile);
        }
        return hosts;
    }

    private static BlockPosition position(BlockPos pos) {
        return new BlockPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    private static BlockPos blockPos(BlockPosition position) {
        return new BlockPos(position.x(), position.y(), position.z());
    }
}
