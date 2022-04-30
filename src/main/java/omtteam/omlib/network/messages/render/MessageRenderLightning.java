package omtteam.omlib.network.messages.render;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.render.RenderManager;
import omtteam.omlib.api.render.object.Lightning;
import omtteam.omlib.util.RandomUtil;

/**
 * Created by nico on 23/05/17.
 * This gives the client a Lightning object to be rendered in the next RenderWorldLastEvent.
 */
public class MessageRenderLightning implements IMessage {
    private double x, y, z, xEnd, yEnd, zEnd;
    private int duration;

    public MessageRenderLightning() {
    }

    public MessageRenderLightning(double x, double y, double z, double xEnd, double yEnd, double zEnd, int duration) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.zEnd = zEnd;
        this.duration = duration;
    }

    public MessageRenderLightning(Vec3d start, Vec3d end, int duration) {
        this.x = start.x;
        this.y = start.y;
        this.z = start.z;
        this.xEnd = end.x;
        this.yEnd = end.y;
        this.zEnd = end.z;
        this.duration = duration;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.xEnd = buf.readDouble();
        this.yEnd = buf.readDouble();
        this.zEnd = buf.readDouble();
        this.duration = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(xEnd);
        buf.writeDouble(yEnd);
        buf.writeDouble(zEnd);
        buf.writeInt(duration);
    }

    public static class MessageHandlerRenderLightning implements IMessageHandler<MessageRenderLightning, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageRenderLightning messageIn, MessageContext ctx) {
            final MessageRenderLightning message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> RenderManager.getInstance()
                    .addRenderObjectToList(new Lightning(new Vec3d(message.x, message.y, message.z),
                                                         new Vec3d(message.xEnd, message.yEnd, message.zEnd),
                                                         message.duration, RandomUtil.random.nextLong())));
            return null;
        }
    }
}
