package omtteam.omlib.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import omtteam.omlib.render.RenderBlockStateContainer;
import omtteam.omlib.tileentity.ICamoSupport;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 31/01/17.
 * This Class
 */
public abstract class BlockAbstractCamoTileEntity extends BlockAbstractTileEntity {
    public static final IUnlistedProperty<RenderBlockStateContainer> RENDERBLOCKSTATE = new IUnlistedProperty<RenderBlockStateContainer>() {

        public String getName() {
            return "openmodularturrets:camo_property";
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
    public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        ICamoSupport camoTE = (ICamoSupport) world.getTileEntity(pos);
        return ((IExtendedBlockState) state).withProperty(RENDERBLOCKSTATE, new RenderBlockStateContainer(camoTE.getCamoState()));
    }
}
