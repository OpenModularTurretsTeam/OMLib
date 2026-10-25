package omtteam.omlib.compat.valkyrien;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.TransformType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipFramesTest {
    private static final Vec3d POINT = new Vec3d(1.5D, 64D, -3D);

    /**
     * A ship turned by 180° around y and moved 1000 blocks east; this transform is its own inverse, so both
     * directions use the same formula.
     */
    private static final IPhysicsEntity SHIP = new IPhysicsEntity() {
        @Override
        public Vec3d rotateVector(Vec3d vector, TransformType transformType) {
            return new Vec3d(-vector.x, vector.y, -vector.z);
        }

        @Override
        public Vec3d transformVector(Vec3d vector, TransformType transformType) {
            return new Vec3d(1000D - vector.x, vector.y, -vector.z);
        }
    };

    @Test
    void withoutShipModEveryBlockIsInTheWorld() {
        assertSame(ShipFrame.WORLD, ShipFrames.at(null, BlockPos.ORIGIN));
    }

    @Test
    void theWorldFrameConvertsNothing() {
        ShipFrame frame = ShipFrame.WORLD;
        assertFalse(frame.isOnShip());
        assertSame(POINT, frame.toLocal(POINT));
        assertSame(POINT, frame.toWorld(POINT));
        assertSame(POINT, frame.rotateToLocal(POINT));
        assertSame(POINT, frame.rotateToWorld(POINT));
    }

    @Test
    void aShipFrameConvertsBothWays() {
        ShipFrame frame = new ValkyrienShipFrame(SHIP);
        assertTrue(frame.isOnShip());
        Vec3d world = frame.toWorld(POINT);
        assertEquals(new Vec3d(998.5D, 64D, 3D), world);
        assertEquals(POINT, frame.toLocal(world));
        assertEquals(new Vec3d(-1.5D, 64D, 3D), frame.rotateToWorld(POINT));
        assertEquals(POINT, frame.rotateToLocal(frame.rotateToWorld(POINT)));
    }

    @Test
    void aShipFrameAsksForTheRightDirection() {
        TransformType[] last = new TransformType[1];
        ShipFrame frame = new ValkyrienShipFrame(new IPhysicsEntity() {
            @Override
            public Vec3d rotateVector(Vec3d vector, TransformType transformType) {
                last[0] = transformType;
                return vector;
            }

            @Override
            public Vec3d transformVector(Vec3d vector, TransformType transformType) {
                last[0] = transformType;
                return vector;
            }
        });
        frame.toWorld(POINT);
        assertEquals(TransformType.SUBSPACE_TO_GLOBAL, last[0]);
        frame.toLocal(POINT);
        assertEquals(TransformType.GLOBAL_TO_SUBSPACE, last[0]);
        frame.rotateToWorld(POINT);
        assertEquals(TransformType.SUBSPACE_TO_GLOBAL, last[0]);
        frame.rotateToLocal(POINT);
        assertEquals(TransformType.GLOBAL_TO_SUBSPACE, last[0]);
    }
}
