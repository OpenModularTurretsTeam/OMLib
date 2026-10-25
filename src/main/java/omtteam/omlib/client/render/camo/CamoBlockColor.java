package omtteam.omlib.client.render.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.util.world.WorldUtil;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 31/01/17.
 * This Class maps the camo state for the camo blocks to vanilla block colors.
 */
public class CamoBlockColor implements IBlockColor {
    @Override
    public int colorMultiplier(@Nullable IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        TileEntity tileEntity = worldIn != null && pos != null ? WorldUtil.getTileEntity(worldIn, pos) : null;
        if (tileEntity instanceof ICamoSupport && state != ((ICamoSupport) tileEntity).getCamoState()) {
            IBlockState camoState = ((ICamoSupport) tileEntity).getCamoState();
            return Minecraft.getMinecraft().getBlockColors().colorMultiplier(camoState, worldIn, pos, tintIndex);
        }
        return -1;
    }
}