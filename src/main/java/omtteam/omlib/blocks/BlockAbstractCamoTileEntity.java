package omtteam.omlib.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import omtteam.omlib.render.RenderBlockStateContainer;
import omtteam.omlib.tileentity.ICamoSupport;

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

    protected BlockAbstractCamoTileEntity(Material material) {
        super(material);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ICamoSupport camoTE = (ICamoSupport) world.getTileEntity(pos);
        if (camoTE != null) {
            return ((IExtendedBlockState) state).withProperty(RENDERBLOCKSTATE, new RenderBlockStateContainer(camoTE.getCamoState()));
        } else {
            return ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation("minecraft:grass")).getDefaultState();
        }
    }
}
