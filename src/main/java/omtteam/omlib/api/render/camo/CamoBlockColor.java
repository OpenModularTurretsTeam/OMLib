package omtteam.omlib.api.render.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 31/01/17.
 * This Class maps the camo state for the camo blocks to vanilla block colors.
 */
public class CamoBlockColor implements IBlockColor {
    @SuppressWarnings("ConstantConditions")
    @Override
    public int colorMultiplier(@Nullable IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (worldIn != null && pos != null && worldIn.getTileEntity(pos) instanceof ICamoSupport && state != ((ICamoSupport) worldIn.getTileEntity(pos)).getCamoState()) {
            IBlockState camoState = ((ICamoSupport) worldIn.getTileEntity(pos)).getCamoState();
            return Minecraft.getMinecraft().getBlockColors().colorMultiplier(camoState, worldIn, pos, tintIndex);
        }
        return -1;
    }
}