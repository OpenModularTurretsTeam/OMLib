package omtteam.omlib.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilTest {
    /** A turret hit box: centred, long along z (facing south). */
    private static final AxisAlignedBB TURRET = new AxisAlignedBB(-0.3D, -0.2D, -0.5D, 0.3D, 0.2D, 0.5D);
    private static final double EPSILON = 1E-9;

    /** Half the size of the box along x, y and z. */
    private static void assertHalfSizes(EnumFacing facing, double x, double y, double z) {
        AxisAlignedBB box = MathUtil.rotateAABB(TURRET, facing);
        assertEquals(x, (box.maxX - box.minX) / 2, EPSILON, facing + " x");
        assertEquals(y, (box.maxY - box.minY) / 2, EPSILON, facing + " y");
        assertEquals(z, (box.maxZ - box.minZ) / 2, EPSILON, facing + " z");
    }

    @Test
    void southIsTheModelledFacing() {
        assertEquals(TURRET, MathUtil.rotateAABB(TURRET, EnumFacing.SOUTH));
    }

    @Test
    void theLongSideFollowsTheFacing() {
        assertHalfSizes(EnumFacing.SOUTH, 0.3D, 0.2D, 0.5D);
        assertHalfSizes(EnumFacing.NORTH, 0.3D, 0.2D, 0.5D);
        assertHalfSizes(EnumFacing.EAST, 0.5D, 0.2D, 0.3D);
        assertHalfSizes(EnumFacing.WEST, 0.5D, 0.2D, 0.3D);
        assertHalfSizes(EnumFacing.UP, 0.2D, 0.5D, 0.3D);
        assertHalfSizes(EnumFacing.DOWN, 0.2D, 0.5D, 0.3D);
    }

    @Test
    void theBoxStaysCentred() {
        for (EnumFacing facing : EnumFacing.values()) {
            AxisAlignedBB box = MathUtil.rotateAABB(TURRET, facing);
            assertEquals(0D, box.getCenter().x, EPSILON, facing.toString());
            assertEquals(0D, box.getCenter().y, EPSILON, facing.toString());
            assertEquals(0D, box.getCenter().z, EPSILON, facing.toString());
        }
    }
}
