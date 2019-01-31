package omtteam.omlib.api.render.camo;

import net.minecraft.block.state.IBlockState;

/**
 * Created by Keridos on 29/01/17.
 * This is the BlockState saving property for block rendering for camo blocks.
 */
public class RenderBlockStateContainer {
    private IBlockState renderState;

    public RenderBlockStateContainer(IBlockState state) {
        renderState = state;
    }

    public IBlockState getRenderState() {
        return renderState;
    }

    public void setRenderState(IBlockState renderState) {
        this.renderState = renderState;
    }

    @Override
    public String toString() {
        return renderState.toString();
    }
}
