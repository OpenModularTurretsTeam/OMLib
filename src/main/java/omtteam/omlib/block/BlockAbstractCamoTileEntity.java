package omtteam.omlib.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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
        ICamoSupport camoTE = (ICamoSupport) world.getTileEntity(pos);
        if (camoTE != null && state.getBlock() instanceof BlockAbstractCamoTileEntity && state instanceof IExtendedBlockState) {
            IBlockState camoState = camoTE.getCamoState();
            return ((IExtendedBlockState) state)
                    .withProperty(RENDERBLOCKSTATE, new RenderBlockStateContainer(camoState));
        } else if (camoTE != null && !(state instanceof IExtendedBlockState)) {
            return state.getBlock().getExtendedState(state, world, pos);
        }
        return (ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation("minecraft:grass"))).getDefaultState();
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @ParametersAreNonnullByDefault
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }
}
