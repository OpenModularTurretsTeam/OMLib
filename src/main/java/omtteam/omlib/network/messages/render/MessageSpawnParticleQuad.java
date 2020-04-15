package omtteam.omlib.network.messages.render;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.proxy.ClientProxy;
import omtteam.omlib.util.RandomUtil;

import java.util.Objects;

/**
 * Created by nico on 23/05/17.
 * This gives the client a Ray object to be rendered in the next RenderWorldLastEvent.
 */
public class MessageSpawnParticleQuad implements IMessage {
    private double x, y, z, xRange, yRange, zRange, speedFactor;
    private int amount, id, dimensionId;

    public MessageSpawnParticleQuad() {
    }

    public MessageSpawnParticleQuad(int id, int dimensionId, double x, double y, double z, double xRange, double yRange, double zRange, double speedFactor, int amount) {
        this.id = id;
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRange = xRange;
        this.yRange = yRange;
        this.zRange = zRange;
        this.speedFactor = speedFactor;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readInt();
        this.dimensionId = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.xRange = buf.readDouble();
        this.yRange = buf.readDouble();
        this.zRange = buf.readDouble();
        this.speedFactor = buf.readDouble();
        this.amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(dimensionId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(xRange);
        buf.writeDouble(yRange);
        buf.writeDouble(zRange);
        buf.writeDouble(speedFactor);
        buf.writeInt(amount);
    }

    public static class MessageHandlerSpawnParticleQuad implements IMessageHandler<MessageSpawnParticleQuad, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageSpawnParticleQuad messageIn, MessageContext ctx) {
            final MessageSpawnParticleQuad message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = ClientProxy.getWorld(FMLClientHandler.instance().getClient());
                if (world.provider.getDimension() != message.dimensionId) {
                    return;
                }
                for (int i = 0; i <= message.amount; i++) {
                    try {
                        world.spawnParticle(Objects.requireNonNull(EnumParticleTypes.getParticleFromId(message.id)),
                                            message.x + (RandomUtil.random.nextDouble() - 0.5) * message.xRange,
                                            message.y + (RandomUtil.random.nextDouble() - 0.5) * message.yRange,
                                            message.z + (RandomUtil.random.nextDouble() - 0.5) * message.zRange,
                                            (RandomUtil.random.nextDouble() - 0.5) * message.speedFactor,
                                            (RandomUtil.random.nextDouble() - 0.5) * message.speedFactor,
                                            (RandomUtil.random.nextDouble() - 0.5) * message.speedFactor);
                    } catch (Exception e) {
                        return;
                    }
                }
            });
            return null;
        }
    }
}
