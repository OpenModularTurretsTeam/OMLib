package valkyrienwarfare.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Compile-only stub of the ship API shared by Valkyrien Skies 1 and Rifted Valkyrium (Apache License 2.0, by
 * thebest108 and the Valkyrien Skies / Rift Modding Group authors), reduced to what OMLib uses. Not part of the
 * jar: the ship mod provides the real class (Rifted Valkyrium is compiled for Java 25, so Java 8 cannot compile
 * against its jar).
 */
public interface IPhysicsEntityManager {
    /** Replaced at runtime by the ship mod. */
    IPhysicsEntityManager INSTANCE = null;

    /**
     * The ship the block position belongs to, or {@code null} if the position is not in ship space.
     */
    @Nullable
    IPhysicsEntity getPhysicsEntityFromShipSpace(World world, BlockPos pos);
}
