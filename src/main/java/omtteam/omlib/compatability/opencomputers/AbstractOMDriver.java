package omtteam.omlib.compatability.opencomputers;

import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by nico on 09/06/17.
 */
public abstract class AbstractOMDriver extends DriverSidedTileEntity {
    @Override
    public Class<?> getTileEntityClass() {
        return clGetTileEntityClass();
    }

    protected abstract Class<?> clGetTileEntityClass() ;

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        return clCreateEnvironment(world, pos, side);
    }

    protected abstract ManagedEnvironment clCreateEnvironment(World world, BlockPos pos, EnumFacing side);
}
