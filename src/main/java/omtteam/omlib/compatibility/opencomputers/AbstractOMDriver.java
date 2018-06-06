package omtteam.omlib.compatibility.opencomputers;


import li.cil.oc.api.Driver;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.OMLib;

/**
 * Created by nico on 09/06/17.
 */
public abstract class AbstractOMDriver {

    protected abstract Class<?> clGetTileEntityClass();

    protected abstract ManagedEnvironment clCreateEnvironment(World world, BlockPos pos, EnumFacing side);

    protected abstract String getName();

    public void registerWrapper() {
        Driver.add(new DriverSidedTEWrapper());
        OMLib.getLogger().info("Registered OC Driver: " + this.getName());
    }

    private class DriverSidedTEWrapper extends DriverSidedTileEntity {
        @Override
        public Class<?> getTileEntityClass() {
            return clGetTileEntityClass();
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
            return clCreateEnvironment(world, pos, side);
        }
    }
}
