package omtteam.omlib.render;

import net.minecraftforge.common.property.IExtendedBlockState;

/**
 * Created by Keridos on 29/01/17.
 * This is the BlockState saving property for block rendering for camo blocks.
 */
public class RenderBlockStateContainer {
    private IExtendedBlockState renderState;

    public RenderBlockStateContainer(IExtendedBlockState state) {
        renderState = state;
    }

    public IExtendedBlockState getRenderState() {
        return renderState;
    }

    public void setRenderState(IExtendedBlockState renderState) {
        this.renderState = renderState;
    }

    @Override
    public String toString() {
        return renderState.toString();
    }
}
