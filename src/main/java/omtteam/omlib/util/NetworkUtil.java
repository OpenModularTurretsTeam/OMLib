package omtteam.omlib.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class NetworkUtil {
    public static NetworkRegistry.TargetPoint getTargetPointFromTE(TileEntity te, double range) {
        return new NetworkRegistry.TargetPoint(te.getWorld().provider.getDimension(),
                                               te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), range);
    }
}
