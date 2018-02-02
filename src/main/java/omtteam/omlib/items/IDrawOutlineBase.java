package omtteam.omlib.items;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 06/08/17.
 * This Class
 */
public interface IDrawOutlineBase extends IDrawOutline {
    @Nullable
    EnumFacing getBaseFacing(World world, BlockPos pos);
}
