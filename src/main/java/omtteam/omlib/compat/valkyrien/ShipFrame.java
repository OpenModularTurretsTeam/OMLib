package omtteam.omlib.compat.valkyrien;

import net.minecraft.util.math.Vec3d;

/**
 * The coordinate frame of a block: world space, or the ship space of a Rifted Valkyrium / Valkyrien Skies 1 ship
 * the block is part of. A ship's blocks keep their positions in ship space while the ship moves and turns in the
 * world; code that compares a block with things in the world (entities, sounds the ship mod does not move, rays)
 * converts through this frame. Get one with {@link ShipFrames#at}, use it within the tick and do not keep it: the
 * ship moves every tick.
 */
public interface ShipFrame {
    /** The frame of blocks that are not on a ship: every conversion returns its input. */
    ShipFrame WORLD = new WorldFrame();

    /** Whether the block is on a ship (otherwise this is {@link #WORLD}). */
    boolean isOnShip();

    /** A position in the world, in this frame. */
    Vec3d toLocal(Vec3d worldPos);

    /** A position in this frame, in the world. */
    Vec3d toWorld(Vec3d localPos);

    /** A direction or velocity in the world, in this frame. */
    Vec3d rotateToLocal(Vec3d worldDir);

    /** A direction or velocity in this frame, in the world. */
    Vec3d rotateToWorld(Vec3d localDir);
}
