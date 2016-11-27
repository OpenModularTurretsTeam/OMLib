package omtteam.omlib.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Created by Keridos on 20/07/16.
 * This Class
 */
public class WorldUtil {
    public static ArrayList<TileEntity> getTouchingTileEntities(World world, BlockPos pos){
        ArrayList<TileEntity> list = new ArrayList<>();
        for (EnumFacing facing: EnumFacing.VALUES) {
            list.add(world.getTileEntity(pos.offset(facing)));
        }
        return list;
    }

    public static ArrayList<IBlockState> getTouchingBlockStates(World world, BlockPos pos){
        ArrayList<IBlockState> list = new ArrayList<>();
        for (EnumFacing facing: EnumFacing.VALUES) {
            list.add(world.getBlockState(pos.offset(facing)));
        }
        return list;
    }
}
