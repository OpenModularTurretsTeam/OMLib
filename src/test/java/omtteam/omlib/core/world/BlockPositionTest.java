package omtteam.omlib.core.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockPositionTest {
    @Test
    void keepsItsCoordinates() {
        BlockPosition pos = new BlockPosition(3, -7, 11);
        assertEquals(3, pos.x());
        assertEquals(-7, pos.y());
        assertEquals(11, pos.z());
    }

    @Test
    void offsetAddsPerAxis() {
        assertEquals(new BlockPosition(5, -4, 8), new BlockPosition(3, -7, 11).offset(2, 3, -3));
    }
}
