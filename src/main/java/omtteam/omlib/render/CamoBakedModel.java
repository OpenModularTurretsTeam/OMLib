package omtteam.omlib.render;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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
public abstract class CamoBakedModel implements IBakedModel {
    protected static ResourceLocation FAKE_LOCATION;
    private final List<IBakedModel> defaultModels;

    public CamoBakedModel(List<IBakedModel> list) {
        defaultModels = list;
    }
    
    /**
     * Returns the corresponding model used for rendering the default model of the block.
     *
     * @param   list    the list of available models
     * @param   state   the state to get the model for
     * @return the correct model for the given state
     */
    protected abstract IBakedModel getModel(List<IBakedModel> list, IBlockState state);

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        IBlockState camoState = extendedState.getValue(RENDERBLOCKSTATE).getRenderState();

        if (camoState.getBlock() instanceof BlockAbstractCamoTileEntity && state != null) {
            return getModel(defaultModels, state).getQuads(state, side, rand);

        } else {
            return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(camoState).getQuads(camoState, side, rand);
        }
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
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}
