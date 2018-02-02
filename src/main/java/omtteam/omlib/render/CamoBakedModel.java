package omtteam.omlib.render;


import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.blocks.BlockAbstractCamoTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static omtteam.omlib.blocks.BlockAbstractCamoTileEntity.RENDERBLOCKSTATE;


/**
 * Created by Keridos on 29/01/17.
 * This Class is the abstract implementation of the camo model.
 * Extending classes must implement 3 additional classes that manage the default models caching and statemapping.
 * See TurretBaseBakedModel in OMT for a reference implementation.
 */
@MethodsReturnNonnullByDefault
public abstract class CamoBakedModel implements IBakedModel {
    private final List<IBakedModel> defaultModels;

    public CamoBakedModel(List<IBakedModel> list) {
        defaultModels = list;
    }

    /**
     * Returns the corresponding model used for rendering the default model of the block.
     *
     * @param list  the list of available models
     * @param state the state to get the model for
     * @return the correct model for the given state
     */
    protected abstract IBakedModel getModel(List<IBakedModel> list, @Nullable IBlockState state);

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        IBlockState camoState = null;
        if (extendedState != null) {
            camoState = extendedState.getValue(RENDERBLOCKSTATE).getRenderState();
        }

        if (camoState != null && camoState.getBlock() instanceof BlockAbstractCamoTileEntity) {
            return getModel(defaultModels, state).getQuads(state, side, rand);

        } else if (camoState != null ){
            return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(camoState).getQuads(camoState, side, rand);
        }
        return getModel(defaultModels, BlockAbstractCamoTileEntity.getStateById(0)).getQuads(BlockAbstractCamoTileEntity.getStateById(0), side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public abstract TextureAtlasSprite getParticleTexture();

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
