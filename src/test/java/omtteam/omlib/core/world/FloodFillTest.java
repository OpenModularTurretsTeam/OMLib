package omtteam.omlib.core.world;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloodFillTest {
    private static final BlockPosition ORIGIN = new BlockPosition(0, 64, 0);

    /** A straight line of {@code length} blocks along +x, starting at {@link #ORIGIN}. */
    private static Set<BlockPosition> line(int length) {
        Set<BlockPosition> blocks = new HashSet<>();
        for (int i = 0; i < length; i++) {
            blocks.add(new BlockPosition(i, 64, 0));
        }
        return blocks;
    }

    private static Set<BlockPosition> setOf(BlockPosition... positions) {
        return new HashSet<>(Arrays.asList(positions));
    }

    @Test
    void startThatDoesNotJoinFindsNothing() {
        assertEquals(Collections.emptyList(), FloodFill.collect(ORIGIN, pos -> false, 10));
    }

    @Test
    void nonPositiveLimitFindsNothing() {
        assertEquals(Collections.emptyList(), FloodFill.collect(ORIGIN, pos -> true, 0));
    }

    @Test
    void followsFacesOnly() {
        BlockPosition above = ORIGIN.offset(0, 1, 0);
        Set<BlockPosition> blocks = setOf(ORIGIN, above, new BlockPosition(1, 65, 0), new BlockPosition(5, 64, 0));
        List<BlockPosition> found = FloodFill.collect(ORIGIN, blocks::contains, 10);
        assertEquals(setOf(ORIGIN, above, new BlockPosition(1, 65, 0)), new HashSet<>(found),
                "connected through faces, the separate block is left out");
        assertEquals(found.size(), new HashSet<>(found).size(), "no position twice");
    }

    @Test
    void diagonalNeighboursAreNotConnected() {
        Set<BlockPosition> blocks = setOf(ORIGIN, new BlockPosition(1, 65, 0));
        assertEquals(Collections.singletonList(ORIGIN), FloodFill.collect(ORIGIN, blocks::contains, 10));
    }

    @Test
    void nearestFirstAndCutAtTheLimit() {
        Set<BlockPosition> blocks = line(10);
        List<BlockPosition> found = FloodFill.collect(new BlockPosition(4, 64, 0), blocks::contains, 3);
        assertEquals(3, found.size());
        assertEquals(new BlockPosition(4, 64, 0), found.get(0));
        assertEquals(setOf(new BlockPosition(3, 64, 0), new BlockPosition(5, 64, 0)),
                new HashSet<>(found.subList(1, 3)), "the direct neighbours come before anything further away");
    }

    @Test
    void findsAllWithinTheLimit() {
        Set<BlockPosition> blocks = line(50);
        assertEquals(blocks, new HashSet<>(FloodFill.collect(ORIGIN, blocks::contains, 1024)));
    }

    @Test
    void asksEachPositionAtMostOnce() {
        Set<BlockPosition> blocks = new HashSet<>();
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                blocks.add(new BlockPosition(x, 64, z));
            }
        }
        Set<BlockPosition> asked = new HashSet<>();
        FloodFill.collect(ORIGIN, pos -> {
            assertTrue(asked.add(pos), "asked twice: " + pos);
            return blocks.contains(pos);
        }, 1024);
    }
}
