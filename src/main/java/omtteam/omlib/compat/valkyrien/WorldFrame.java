package omtteam.omlib.compat.valkyrien;

import net.minecraft.util.math.Vec3d;

/** {@link ShipFrame#WORLD}: no ship, no conversion. */
final class WorldFrame implements ShipFrame {
    @Override
    public boolean isOnShip() {
        return false;
    }

    @Override
    public Vec3d toLocal(Vec3d worldPos) {
        return worldPos;
    }

    @Override
    public Vec3d toWorld(Vec3d localPos) {
        return localPos;
    }

    @Override
    public Vec3d rotateToLocal(Vec3d worldDir) {
        return worldDir;
    }

    @Override
    public Vec3d rotateToWorld(Vec3d localDir) {
        return localDir;
    }
}
