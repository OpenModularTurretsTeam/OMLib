package omtteam.omlib.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class NetworkUtil {
    public static NetworkRegistry.TargetPoint getTargetPointFromTE(TileEntity te, double range) {
        return getTargetPointFromBlockPos(te.getWorld().provider.getDimension(), te.getPos(), range);
    }

    public static NetworkRegistry.TargetPoint getTargetPointFromBlockPos(int dimension, BlockPos pos, double range) {
        return new NetworkRegistry.TargetPoint(dimension, pos.getX(), pos.getY(), pos.getZ(), range);
    }
}
