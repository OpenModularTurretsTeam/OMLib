package omtteam.omlib.api.render.object;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class Ray extends RenderObject {
    private double xEnd, yEnd, zEnd;
    private float r, g, b;
    private boolean bloom;

    public Ray(double x, double y, double z, double xEnd, double yEnd, double zEnd, float r, float g, float b, int duration, boolean bloom) {
        super(x, y, z, duration);
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.zEnd = zEnd;
        this.r = r;
        this.g = g;
        this.b = b;
        this.bloom = bloom;
    }

    public void render(RenderGlobal renderGlobal, float partialTick) {//TODO: bloom
        try {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.glLineWidth(4.0F);
            GlStateManager.disableLighting();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                                GlStateManager.SourceFactor.ONE,
                                                GlStateManager.DestFactor.ZERO);
            Entity rVE = Minecraft.getMinecraft().getRenderViewEntity();
            if (rVE != null) {

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder worldRenderer = tessellator.getBuffer();

                worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                worldRenderer.pos(x - rVE.posX, y - rVE.posY, z - rVE.posZ).color(r, g, b, 0.7F).endVertex();
                worldRenderer.pos(xEnd - rVE.posX, yEnd - rVE.posY, zEnd - rVE.posZ).color(r, g, b, 0.7F).endVertex();
                tessellator.draw();
            }
        } finally {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}
