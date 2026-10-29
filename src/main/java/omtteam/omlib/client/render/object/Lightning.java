package omtteam.omlib.client.render.object;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.render.ColorOM;
import omtteam.omlib.network.MessageRenderLightning;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class Lightning extends RenderObject {
    private final Vec3d start, end;
    private final ColorOM color;
    private final long randomSeed;

    public Lightning(Vec3d start, Vec3d end, int duration, long randomSeed) {
        this(start, end, MessageRenderLightning.DEFAULT_COLOR, duration, randomSeed);
    }

    public Lightning(Vec3d start, Vec3d end, ColorOM color, int duration, long randomSeed) {
        super(start.x, start.y, start.z, duration);
        this.start = start;
        this.end = end;
        this.color = color;
        this.randomSeed = randomSeed;
    }

    public void render(RenderGlobal renderGlobal, float partialTick) {
        duration -= partialTick;
        try {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.glLineWidth(1.5F);
            GlStateManager.disableLighting();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                                GlStateManager.SourceFactor.ONE,
                                                GlStateManager.DestFactor.ZERO);
            Entity rVE = Minecraft.getMinecraft().getRenderViewEntity();
            if (rVE != null) {
                Random random = new Random(randomSeed);
                int count = random.nextInt(3) + 3;
                for (int i = 0; i < count; i++) {
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    Vec3d lastPos = start;
                    double distance = start.distanceTo(end);
                    bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                    for (int part = 1; part < 8; part++) {
                        Vec3d offset = end.subtract(lastPos).normalize().scale(distance / 8F);
                        Vec3d path = offset.add(lastPos);

                        double x2 = part == 7 ? end.x : path.x + (random.nextDouble() - 0.5F) * distance / 24f;
                        double y2 = part == 7 ? end.y : path.y + (random.nextDouble() - 0.4F) * distance / 24f;
                        double z2 = part == 7 ? end.z : path.z + (random.nextDouble() - 0.5F) * distance / 24f;

                        bufferbuilder.pos(lastPos.x - rVE.posX, lastPos.y - rVE.posY, lastPos.z - rVE.posZ).color(color.r, color.g, color.b, color.a).endVertex();
                        bufferbuilder.pos(x2 - rVE.posX, y2 - rVE.posY, z2 - rVE.posZ).color(color.r, color.g, color.b, color.a).endVertex();

                        lastPos = new Vec3d(x2, y2, z2);
                    }
                    tessellator.draw();
                }
            }
        } catch (Exception e) {
            OMLib.getLogger().error("Failed to render lightning", e);
        } finally {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}

