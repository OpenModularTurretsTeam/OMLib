package omtteam.omlib.handler;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.network.OMLibNetwork;
import omtteam.omlib.api.render.RenderManager;
import omtteam.omlib.init.OMLibBlocks;
import omtteam.omlib.init.OMLibItems;
import omtteam.omlib.items.IDrawOutline;
import omtteam.omlib.items.IDrawOutlineBase;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.network.messages.MessageSetSharePlayerList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static omtteam.omlib.util.RenderUtil.drawHighlightBox;

/**
 * Created by Keridos on 17/05/17.
 * This Class is the OMLibEventHandler for OMLib
 */
public class OMLibEventHandler {
    private static OMLibEventHandler instance;
    private List<OMLibNetwork> networks = new ArrayList<>();

    private OMLibEventHandler() {
    }

    public static OMLibEventHandler getInstance() {
        if (instance == null) {
            instance = new OMLibEventHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public void worldLoadEvent(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            OwnerShareHandler.loadFromDisk();
        }
    }

    @SubscribeEvent
    public void worldUnloadEvent(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            OwnerShareHandler.saveToDisk();
        }
    }

    @SubscribeEvent
    public void playerJoinEvent(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP) {
            OMLibNetworkingHandler.sendMessageToPlayer(new MessageSetSharePlayerList(OwnerShareHandler.getInstance()), (EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    public void itemRegisterEvent(RegistryEvent.Register<Item> event) {
        OMLibItems.init(event.getRegistry());
    }

    @SubscribeEvent
    public void blockRegisterEvent(RegistryEvent.Register<Block> event) {
        OMLibBlocks.initBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public void renderRegisterEvent(ModelRegistryEvent event) {
        OMLib.proxy.initRenderers();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void drawBlockOutline(DrawBlockHighlightEvent event) {
        if (event.getTarget() != null && event.getTarget().sideHit != null && event.getPlayer().getHeldItemMainhand() != null && event.getPlayer().getHeldItemMainhand().getItem() instanceof IDrawOutline) {
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
            if (event.getPlayer().getEntityWorld().isAirBlock(blockpos)) {
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
    public void tickEvent(TickEvent.WorldTickEvent event) {
        for (OMLibNetwork network : networks) {
            if (network.getWorld().provider.getDimension() == event.world.provider.getDimension()) {
                network.tick();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        RenderManager.getInstance().renderWorldLastEvent(event.getContext(), event.getPartialTicks());
    }

    private List<OMLibNetwork> getNetworkList() {
        return networks;
    }

    public void registerNetwork(OMLibNetwork network) {
        networks.add(network);
    }

    public void removeNetwork(OMLibNetwork network) {
        networks.remove(network);
    }

    @Nullable
    public OMLibNetwork getNetwork(UUID uuid) {
        OMLibNetwork network = null;
        for (OMLibNetwork net : networks) {
            if (net.getUuid().equals(uuid)) {
                network = net;
                break;
            }
        }
        return network;
    }
}

