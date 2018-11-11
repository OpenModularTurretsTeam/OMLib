package omtteam.omlib.api.render.object;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class Ray extends RenderObject {
    private double xEnd, yEnd, zEnd;
    private float r, g, b;
    private boolean bloom;

    public Ray(double x, double y, double z, double xEnd, double yEnd, double zEnd, float r, float g, float b, boolean bloom) {
        super(x, y, z);
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
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glPushMatrix();
            GL11.glColor3f(1F, 1F, 0.0F);
            GL11.glLineWidth(4.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glTranslatef((float) x, (float) y, (float) z);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder worldRenderer = tessellator.getBuffer();

            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(x, y, z).color(r, g, b, 0.7F).endVertex();
            worldRenderer.pos(xEnd, yEnd, zEnd).color(r, g, b, 0.7F).endVertex();
            tessellator.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}
