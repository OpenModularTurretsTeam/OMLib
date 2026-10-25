package valkyrienwarfare.api;

import net.minecraft.util.math.Vec3d;

/**
 * Compile-only stub of a ship (see {@link IPhysicsEntityManager}), reduced to what OMLib uses.
 */
public interface IPhysicsEntity {
    /** Rotates a direction between world and ship space. */
    Vec3d rotateVector(Vec3d vector, TransformType transformType);

    /** Transforms a position between world and ship space. */
    Vec3d transformVector(Vec3d vector, TransformType transformType);
}
