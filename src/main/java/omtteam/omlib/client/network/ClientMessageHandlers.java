package omtteam.omlib.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.client.render.RenderManager;
import omtteam.omlib.client.render.object.Lightning;
import omtteam.omlib.client.render.object.Ray;
import omtteam.omlib.network.MessageCamoSettings;
import omtteam.omlib.network.MessageRenderLightning;
import omtteam.omlib.network.MessageRenderRay;
import omtteam.omlib.network.MessageSetSharePlayerList;
import omtteam.omlib.network.MessageSpawnParticleQuad;
import omtteam.omlib.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The client side of OMLib's server-to-client messages. The client proxy hands them to the network registration
 * ({@code CommonProxy.clientMessageHandler}); a dedicated server registers no-op handlers instead.
 */
@SideOnly(Side.CLIENT)
public final class ClientMessageHandlers {
    private static final Map<Class<?>, IMessageHandler<?, IMessage>> HANDLERS = new HashMap<>();

    static {
        register(MessageSetSharePlayerList.class, ClientMessageHandlers::setSharePlayerList);
        register(MessageCamoSettings.class, ClientMessageHandlers::camoSettings);
        register(MessageRenderRay.class, ClientMessageHandlers::renderRay);
        register(MessageRenderLightning.class, ClientMessageHandlers::renderLightning);
        register(MessageSpawnParticleQuad.class, ClientMessageHandlers::spawnParticleQuad);
    }

    private ClientMessageHandlers() {
    }

    @SuppressWarnings("unchecked")
    public static <M extends IMessage> IMessageHandler<M, IMessage> get(Class<M> type) {
        IMessageHandler<?, IMessage> handler = HANDLERS.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No client handler for " + type.getName());
        }
        return (IMessageHandler<M, IMessage>) handler;
    }

    private static <M extends IMessage> void register(Class<M> type, IMessageHandler<M, IMessage> handler) {
        HANDLERS.put(type, handler);
    }

    private static IMessage setSharePlayerList(MessageSetSharePlayerList message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> OwnerShareRegister.instance.setOwnerShareMap(message.getOwnerShareMap()));
        return null;
    }

    @SuppressWarnings("deprecation")
    private static IMessage camoSettings(MessageCamoSettings message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            TileEntity tileEntity = Minecraft.getMinecraft().world.getTileEntity(message.getPos());
            if (tileEntity instanceof ICamoSupport) {
                ICamoSupport base = (ICamoSupport) tileEntity;
                base.getCamoSettings().setLightValue(message.getLightValue());
                base.getCamoSettings().setLightOpacity(message.getLightOpacity());
                base.setCamoState(Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(
                        new ResourceLocation(message.getCamoBlockRegName()))).getStateFromMeta(message.getCamoBlockMeta()));
                base.getCamoTileEntity().markDirty();
                base.getCamoTileEntity().getWorld().checkLight(base.getCamoTileEntity().getPos());
            }
        });
        return null;
    }

    private static IMessage renderRay(MessageRenderRay message, MessageContext ctx) {
        Vec3d start = message.getStart();
        Vec3d end = message.getEnd();
        Minecraft.getMinecraft().addScheduledTask(() -> RenderManager.getInstance().addRenderObjectToList(
                new Ray(start.x, start.y, start.z, end.x, end.y, end.z, message.getColor(), message.getDuration(),
                        message.isBloom())));
        return null;
    }

    private static IMessage renderLightning(MessageRenderLightning message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> RenderManager.getInstance().addRenderObjectToList(
                new Lightning(message.getStart(), message.getEnd(), message.getDuration(), RandomUtil.random.nextLong())));
        return null;
    }

    private static IMessage spawnParticleQuad(MessageSpawnParticleQuad message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().world;
            if (world.provider.getDimension() != message.getDimensionId()) {
                return;
            }
            for (int i = 0; i <= message.getAmount(); i++) {
                try {
                    world.spawnParticle(Objects.requireNonNull(EnumParticleTypes.getParticleFromId(message.getId())),
                                        message.getX() + (RandomUtil.random.nextDouble() - 0.5) * message.getXRange(),
                                        message.getY() + (RandomUtil.random.nextDouble() - 0.5) * message.getYRange(),
                                        message.getZ() + (RandomUtil.random.nextDouble() - 0.5) * message.getZRange(),
                                        (RandomUtil.random.nextDouble() - 0.5) * message.getSpeedFactor(),
                                        (RandomUtil.random.nextDouble() - 0.5) * message.getSpeedFactor(),
                                        (RandomUtil.random.nextDouble() - 0.5) * message.getSpeedFactor());
                } catch (Exception e) {
                    return;
                }
            }
        });
        return null;
    }
}
