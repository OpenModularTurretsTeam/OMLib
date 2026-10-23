package omtteam.omlib.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.api.render.camo.RenderBlockStateContainer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 31/01/17.
 * This Class
 */
public abstract class BlockAbstractCamoTileEntity extends BlockAbstractTileEntity {
    public static final IUnlistedProperty<RenderBlockStateContainer> RENDERBLOCKSTATE = new IUnlistedProperty<RenderBlockStateContainer>() {

        public String getName() {
            return "omlib:camo_property";
        }

        @Override
        public boolean isValid(RenderBlockStateContainer value) {
            return true;
        }

        @Override
        public Class<RenderBlockStateContainer> getType() {
            return RenderBlockStateContainer.class;
        }

        @Override
        public String valueToString(RenderBlockStateContainer value) {
            return value.toString();
        }
    };

    @SuppressWarnings("SameParameterValue")
    protected BlockAbstractCamoTileEntity(Material material) {
        super(material);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) {
            return state;
        }
        // Without a camo tile entity (not loaded yet, or another block's) the block renders as itself
        TileEntity tileEntity = world.getTileEntity(pos);
        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        IBlockState camoState = tileEntity instanceof ICamoSupport ? ((ICamoSupport) tileEntity).getCamoState() : extendedState.getClean();
        return extendedState.withProperty(RENDERBLOCKSTATE, new RenderBlockStateContainer(camoState));
    }

    @Override
    @SideOnly(Side.CLIENT)
    @ParametersAreNonnullByDefault
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }
}
