package omtteam.omlib.item;

import net.minecraft.entity.player.EntityPlayer;
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

    boolean canPlace(EnumFacing facing, World world, BlockPos pos);

    /**
     * Whether {@code player} may place the item here; decides the outline colour (cyan or red). Items whose placement
     * depends on the player (e.g. permissions on the block they attach to) override it.
     */
    default boolean canPlace(EnumFacing facing, World world, BlockPos pos, EntityPlayer player) {
        return canPlace(facing, world, pos);
    }
}
