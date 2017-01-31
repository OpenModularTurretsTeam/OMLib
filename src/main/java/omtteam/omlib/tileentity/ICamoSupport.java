package omtteam.omlib.tileentity;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 31/01/17.
 * This Class adds the interface functions for camo supporting tile entities.
 * The TE should have a private IBlockState variable that is accessed be these functions.
 */
public interface ICamoSupport {
    @Nonnull
    IBlockState getCamoState();
    void setCamoState(IBlockState state);
    IBlockState getDefaultCamoState();
}
