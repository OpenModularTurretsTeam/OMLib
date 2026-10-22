package omtteam.omlib.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.client.gui.GUIBackSystem;
import omtteam.omlib.client.gui.ISupportsBackSystem;
import omtteam.omlib.client.render.RenderManager;
import omtteam.omlib.item.IDrawOutline;
import omtteam.omlib.item.IDrawOutlineBase;

import static omtteam.omlib.client.render.RenderUtil.drawHighlightBox;

/**
 * OMLib's client-only events (models, block outlines, render objects, the GUI back system), registered by the client
 * proxy.
 */
@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public void renderRegisterEvent(ModelRegistryEvent event) {
        ClientProxy.registerItemModels();
    }

    @SubscribeEvent
    public void drawBlockOutline(DrawBlockHighlightEvent event) {
        if (event.getTarget() != null && event.getTarget().sideHit != null && event.getPlayer().getHeldItemMainhand() != ItemStack.EMPTY && event.getPlayer().getHeldItemMainhand().getItem() instanceof IDrawOutline) {
            BlockPos blockpos = event.getTarget().getBlockPos().offset(event.getTarget().sideHit);
            IDrawOutline item = (IDrawOutline) event.getPlayer().getHeldItemMainhand().getItem();

            AxisAlignedBB alignedBB;
            EnumFacing facing;
            if (item instanceof IDrawOutlineBase && ((IDrawOutlineBase) item).getBaseFacing(event.getPlayer().getEntityWorld(), blockpos) != null) {
                facing = ((IDrawOutlineBase) item).getBaseFacing(event.getPlayer().getEntityWorld(), blockpos);
            } else if (item instanceof IDrawOutlineBase) {
                return;
            } else {
                facing = event.getTarget().sideHit.getOpposite();
            }
            alignedBB = item.getRenderOutline(facing, event.getPlayer().getEntityWorld(), blockpos);
            EntityPlayer player = event.getPlayer();
            double partialTicks = event.getPartialTicks();
            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            float[] color = new float[3];
            if (item.canPlace(facing, event.getPlayer().getEntityWorld(), blockpos)) {
                color[0] = 0F;
                color[1] = 1F;
                color[2] = 1F;
            } else {
                color[0] = 1F;
                color[1] = 0F;
                color[2] = 0F;
            }

            alignedBB = alignedBB.offset(-d0, -d1, -d2);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            drawHighlightBox(vertexbuffer, alignedBB.minX, alignedBB.minY, alignedBB.minZ, alignedBB.maxX, alignedBB.maxY, alignedBB.maxZ, color[0], color[1], color[2], 0.5F);
            tessellator.draw();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        RenderManager.getInstance().renderWorldLastEvent(event.getContext(), event.getPartialTicks());
    }

    @SubscribeEvent
    public void playerGUIEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof ISupportsBackSystem && ((ISupportsBackSystem) event.getGui()).getGuiParameters() != null) {
            GUIBackSystem.getInstance().addGuiToStack((ISupportsBackSystem) event.getGui());
        } else if (event.getGui() == null) {
            GUIBackSystem.getInstance().clearStack();
        }
    }
}
