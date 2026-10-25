package omtteam.omlib.compat.valkyrien;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.IPhysicsEntityManager;
import valkyrienwarfare.api.TransformType;

/**
 * A {@link ShipFrame} backed by the ship API ({@code valkyrienwarfare.api}). Only loaded when the ship mod is
 * present: the API classes are not part of OMLib's jar.
 */
final class ValkyrienShipFrame implements ShipFrame {
    private final IPhysicsEntity ship;

    ValkyrienShipFrame(IPhysicsEntity ship) {
        this.ship = ship;
    }

    static ShipFrame at(World world, BlockPos pos) {
        IPhysicsEntity ship = IPhysicsEntityManager.INSTANCE.getPhysicsEntityFromShipSpace(world, pos);
        return ship == null ? ShipFrame.WORLD : new ValkyrienShipFrame(ship);
    }

    @Override
    public boolean isOnShip() {
        return true;
    }

    @Override
    public Vec3d toLocal(Vec3d worldPos) {
        return ship.transformVector(worldPos, TransformType.GLOBAL_TO_SUBSPACE);
    }

    @Override
    public Vec3d toWorld(Vec3d localPos) {
        return ship.transformVector(localPos, TransformType.SUBSPACE_TO_GLOBAL);
    }

    @Override
    public Vec3d rotateToLocal(Vec3d worldDir) {
        return ship.rotateVector(worldDir, TransformType.GLOBAL_TO_SUBSPACE);
    }

    @Override
    public Vec3d rotateToWorld(Vec3d localDir) {
        return ship.rotateVector(localDir, TransformType.SUBSPACE_TO_GLOBAL);
    }
}
