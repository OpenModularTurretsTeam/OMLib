package omtteam.omlib.util;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public class RenderUtil {

    @SideOnly(Side.CLIENT)
    public static void drawHighlightBox(VertexBuffer vertexBuffer, double x, double y, double z, double x1, double y1, double z1, float red, float green, float blue, float alpha) {
        vertexBuffer.pos(x, y, z).color(red, green, blue, 0.0F).endVertex();
        vertexBuffer.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y, z1).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x, y, z1).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x, y1, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y1, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x, y1, z1).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x, y1, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x, y1, z1).color(red, green, blue, 0.0F).endVertex();
        vertexBuffer.pos(x, y, z1).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y1, z1).color(red, green, blue, 0.0F).endVertex();
        vertexBuffer.pos(x1, y, z1).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y1, z).color(red, green, blue, 0.0F).endVertex();
        vertexBuffer.pos(x1, y, z).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(x1, y, z).color(red, green, blue, 0.0F).endVertex();
    }

}
