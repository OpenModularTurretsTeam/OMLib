package omtteam.omlib.core.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Finds the blocks connected to a start block through their faces, e.g. the owned blocks a claim spreads to (1.20.1+
 * OMT {@code core.world.FloodFill}).
 */
public final class FloodFill {
    /** The offsets of the six face neighbours. */
    private static final int[][] FACES = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};

    private FloodFill() {
    }

    /**
     * Positions connected to {@code start} through faces where {@code joins} is true, breadth-first (nearest first),
     * at most {@code limit}. {@code start} is included if it joins; if it does not, the result is empty.
     * {@code joins} is asked at most once per position.
     */
    public static List<BlockPosition> collect(BlockPosition start, Predicate<BlockPosition> joins, int limit) {
        List<BlockPosition> found = new ArrayList<>();
        if (limit <= 0 || !joins.test(start)) return found;
        Set<BlockPosition> seen = new HashSet<>();
        Queue<BlockPosition> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty() && found.size() < limit) {
            BlockPosition pos = queue.remove();
            found.add(pos);
            for (int[] face : FACES) {
                BlockPosition next = pos.offset(face[0], face[1], face[2]);
                if (seen.add(next) && joins.test(next)) {
                    queue.add(next);
                }
            }
        }
        return found;
    }
}
