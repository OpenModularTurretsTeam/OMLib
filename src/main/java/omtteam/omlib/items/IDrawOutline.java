package omtteam.omlib.items;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Keridos on 06/08/17.
 * This Class
 */
public interface IDrawOutline {
    AxisAlignedBB getRenderOutline(EnumFacing facing, World world, BlockPos pos);
}
