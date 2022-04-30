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
import omtteam.omlib.api.render.ColorOM;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class Ray extends RenderObject {
    private final double xEnd;
    private final double yEnd;
    private final double zEnd;
    private final ColorOM color;
    private final boolean bloom;

    public Ray(double x, double y, double z, double xEnd, double yEnd, double zEnd, float r, float g, float b, float alpha, int duration, boolean bloom) {
        super(x, y, z, duration);
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.zEnd = zEnd;
        this.color = new ColorOM(r, g, b, alpha);
        this.bloom = bloom;
    }

    public Ray(double x, double y, double z, double xEnd, double yEnd, double zEnd, ColorOM color, int duration, boolean bloom) {
        super(x, y, z, duration);
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.zEnd = zEnd;
        this.color = color;
        this.bloom = bloom;
    }

    public void render(RenderGlobal renderGlobal, float partialTick) {//TODO: bloom
        duration -= partialTick;
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
                worldRenderer.pos(x - rVE.posX, y - rVE.posY, z - rVE.posZ).color(color.r, color.g, color.b, color.a).endVertex();
                worldRenderer.pos(xEnd - rVE.posX, yEnd - rVE.posY, zEnd - rVE.posZ).color(color.r, color.g, color.b, color.a).endVertex();
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
