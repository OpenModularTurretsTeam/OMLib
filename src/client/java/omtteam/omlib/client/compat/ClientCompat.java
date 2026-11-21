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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;

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
        screen.renderBackground(graphics);
    }

    /** Draws a part of a 256 × 256 GUI texture (1.21.11 also takes the render pipeline and the texture size). */
    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width,
                            int height) {
        graphics.blit(texture, x, y, u, v, width, height);
    }

    /** Shows a tooltip at the mouse; 1.21.11 draws it at the end of the frame. */
    public static void renderTooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY) {
        graphics.renderTooltip(font, text, mouseX, mouseY);
    }

    /** Shows a tooltip of wrapped lines at the mouse. */
    public static void renderTooltip(GuiGraphics graphics, Font font, List<FormattedCharSequence> lines, int mouseX,
                                     int mouseY) {
        graphics.renderTooltip(font, lines, mouseX, mouseY);
    }

    /** Shows a tooltip of several lines at a position. */
    public static void renderComponentTooltip(GuiGraphics graphics, Font font, List<Component> lines, int x, int y) {
        graphics.renderComponentTooltip(font, lines, x, y);
    }

    /**
     * Shows a tooltip-styled box that stays on screen next to other tooltips. Up to 1.21.1 tooltips are drawn at once,
     * so this is a plain tooltip; 1.21.11 keeps only one tooltip per frame, so the box is drawn by hand.
     */
    public static void renderStaticTooltip(GuiGraphics graphics, Font font, List<Component> lines, int x, int y) {
        graphics.renderComponentTooltip(font, lines, x, y);
    }

    /** The render type of lines in the world ({@code RenderTypes} on 1.21.11). */
    public static RenderType lines() {
        return RenderType.lines();
    }

    /** The render type of glowing beams (lightning). */
    public static RenderType lightning() {
        return RenderType.lightning();
    }

    /** Draws the 12 edges of a box into a {@link #lines()} buffer; the colour components go from 0 to 1. */
    public static void renderLineBox(PoseStack poseStack, VertexConsumer lines, double minX, double minY, double minZ,
                                     double maxX, double maxY, double maxZ, float red, float green, float blue,
                                     float alpha) {
        LevelRenderer.renderLineBox(poseStack, lines, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
    }

    /** Whether a shift key is held (1.21.11 asks the game instead of {@code Screen}). */
    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    /** Adds a position + colour vertex (the {@code VertexConsumer} API changed in 1.21). */
    public static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z, int argb) {
        consumer.vertex(pose, x, y, z).color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF)
                .endVertex();
    }

    /**
     * Adds a full entity vertex (position, white colour, UV, no overlay, light, normal) for
     * {@code RenderType.entity*} buffers.
     */
    public static void entityVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                                    int packedLight, float normalX, float normalY, float normalZ) {
        consumer.vertex(pose.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
    }

    /** Partial tick of the frame being drawn (stands still while the game is paused). */
    public static float partialTick() {
        return Minecraft.getInstance().getFrameTime();
    }

    /**
     * Turns the pose towards the camera and scales it for text, like a name tag: x right, y down. The camera rotation
     * changed in 1.20.5, so 1.20.1 mirrors x and 1.21 does not (a mirrored label faces away and is culled).
     */
    public static void faceCamera(PoseStack poseStack, Camera camera, float scale) {
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-scale, -scale, scale);
    }

    private static final long CURSOR_MAX_AGE_MS = 2000;
    private static double savedCursorX;
    private static double savedCursorY;
    private static long savedCursorTime;

    private static long windowHandle() {
        return Minecraft.getInstance().getWindow().getWindow();
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
