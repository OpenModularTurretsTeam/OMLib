package omtteam.omlib.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.shapes.Shapes;

import org.joml.Matrix4f;

import java.util.List;

/**
 * Hides client-side API differences between the supported Minecraft versions.
 */
public final class ClientCompat {
    private ClientCompat() {
    }

    /**
     * Draws the dimmed background of a container screen. Only needed on 1.20.1; on 1.21 {@code Screen.render}
     * already does it.
     */
    @SuppressWarnings("unused")
    public static void renderContainerBackground(Screen screen, GuiGraphics graphics, int mouseX, int mouseY,
                                                 float partialTick) {
    }

    /** Draws a part of a 256 × 256 GUI texture (1.21.11 also takes the render pipeline and the texture size). */
    public static void blit(GuiGraphics graphics, Identifier texture, int x, int y, int u, int v, int width,
                            int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
    }

    /** Shows a tooltip at the mouse; 1.21.11 draws it at the end of the frame. */
    public static void renderTooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY) {
        graphics.setTooltipForNextFrame(font, text, mouseX, mouseY);
    }

    /** Shows a tooltip of wrapped lines at the mouse. */
    public static void renderTooltip(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int mouseX,
                                     int mouseY) {
        graphics.setTooltipForNextFrame(font, lines, mouseX, mouseY);
    }

    /** Shows a tooltip of several lines at a position. */
    public static void renderComponentTooltip(GuiGraphics graphics, Font font, List<Component> lines, int x, int y) {
        graphics.setComponentTooltipForNextFrame(font, lines, x, y);
    }

    /**
     * Shows a tooltip-styled box that stays on screen next to other tooltips. Up to 1.21.1 tooltips are drawn at once,
     * so this is a plain tooltip; 1.21.11 keeps only one tooltip per frame, so the box is drawn by hand.
     */
    public static void renderStaticTooltip(GuiGraphics graphics, Font font, List<Component> lines, int x, int y) {
        int boxX = x + 12;
        int boxY = y - 12;
        int width = 0;
        for (Component line : lines) width = Math.max(width, font.width(line));
        int height = lines.size() * font.lineHeight;
        graphics.fill(boxX - 4, boxY - 4, boxX + width + 4, boxY + height + 4, 0xF0100010);
        graphics.fill(boxX - 3, boxY - 3, boxX + width + 3, boxY + height + 3, 0xF0100010);
        graphics.fill(boxX - 3, boxY - 3, boxX + width + 3, boxY - 2, 0xFF5000FF);
        graphics.fill(boxX - 3, boxY + height + 2, boxX + width + 3, boxY + height + 3, 0xFF28007F);
        graphics.fill(boxX - 3, boxY - 3, boxX - 2, boxY + height + 3, 0xFF5000FF);
        graphics.fill(boxX + width + 2, boxY - 3, boxX + width + 3, boxY + height + 3, 0xFF28007F);
        int lineY = boxY;
        for (Component line : lines) {
            graphics.drawString(font, line, boxX, lineY, 0xFFFFFFFF, true);
            lineY += font.lineHeight;
        }
    }

    /** The render type of lines in the world ({@code RenderTypes} on 1.21.11). */
    public static RenderType lines() {
        return RenderTypes.lines();
    }

    /** The render type of glowing beams (lightning). */
    public static RenderType lightning() {
        return RenderTypes.lightning();
    }

    /** Draws the 12 edges of a box into a {@link #lines()} buffer; the colour components go from 0 to 1. */
    public static void renderLineBox(PoseStack poseStack, VertexConsumer lines, double minX, double minY, double minZ,
                                     double maxX, double maxY, double maxZ, float red, float green, float blue,
                                     float alpha) {
        // 1.21.11 lines carry their width; this is the one of the block outline
        ShapeRenderer.renderShape(poseStack, lines, Shapes.box(minX, minY, minZ, maxX, maxY, maxZ), 0, 0, 0,
                ARGB.colorFromFloat(alpha, red, green, blue), Minecraft.getInstance().getWindow().getAppropriateLineWidth());
    }

    /** Whether a shift key is held (1.21.11 asks the game instead of {@code Screen}). */
    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    /** Adds a position + colour vertex (the {@code VertexConsumer} API changed in 1.21). */
    public static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, int argb) {
        consumer.addVertex(pose, x, y, z).setColor(argb);
    }

    /**
     * Adds a full entity vertex (position, white colour, UV, no overlay, light, normal) for
     * {@code RenderType.entity*} buffers.
     */
    public static void entityVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                                    int packedLight, float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight).setNormal(pose, normalX, normalY, normalZ);
    }

    /** Partial tick of the frame being drawn (stands still while the game is paused). */
    public static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    /**
     * Turns the pose towards the camera and scales it for text, like a name tag: x right, y down. The camera rotation
     * changed in 1.20.5, so 1.20.1 mirrors x and 1.21 does not (a mirrored label faces away and is culled).
     */
    public static void faceCamera(PoseStack poseStack, Camera camera, float scale) {
        poseStack.mulPose(camera.rotation());
        poseStack.scale(scale, -scale, scale);
    }

    private static final long CURSOR_MAX_AGE_MS = 2000;
    private static double savedCursorX;
    private static double savedCursorY;
    private static long savedCursorTime;

    private static long windowHandle() {
        return Minecraft.getInstance().getWindow().handle();
    }

    /**
     * Remembers the mouse position when a GUI is removed; opening the next one recentres the cursor, so that screen
     * puts it back with {@link #restoreCursor()}.
     */
    public static void rememberCursor() {
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(windowHandle(), x, y);
        savedCursorX = x[0];
        savedCursorY = y[0];
        savedCursorTime = System.currentTimeMillis();
    }

    /** Moves the mouse back to where {@link #rememberCursor()} saw it, if that was a moment ago. */
    public static void restoreCursor() {
        if (savedCursorTime == 0) return;
        boolean fresh = System.currentTimeMillis() - savedCursorTime <= CURSOR_MAX_AGE_MS;
        savedCursorTime = 0;
        if (fresh) GLFW.glfwSetCursorPos(windowHandle(), savedCursorX, savedCursorY);
    }
}
