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
import omtteam.omlib.api.render.object.Ray;

/**
 * Created by nico on 23/05/17.
 * This gives the client a Ray object to be rendered in the next RenderWorldLastEvent.
 */
public class MessageRenderRay implements IMessage {
    private double x, y, z, xEnd, yEnd, zEnd;
    private float r, g, b;
    private int duration;
    private boolean bloom;

    public MessageRenderRay() {
    }

    public MessageRenderRay(double x, double y, double z, double xEnd, double yEnd, double zEnd, float r, float g, float b, int duration, boolean bloom) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.zEnd = zEnd;
        this.r = r;
        this.g = g;
        this.b = b;
        this.duration = duration;
        this.bloom = bloom;
    }

    public MessageRenderRay(Vec3d start, Vec3d end, float r, float g, float b, int duration, boolean bloom) {
        this.x = start.x;
        this.y = start.y;
        this.z = start.z;
        this.xEnd = end.x;
        this.yEnd = end.y;
        this.zEnd = end.z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.duration = duration;
        this.bloom = bloom;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.xEnd = buf.readDouble();
        this.yEnd = buf.readDouble();
        this.zEnd = buf.readDouble();
        this.r = buf.readFloat();
        this.g = buf.readFloat();
        this.b = buf.readFloat();
        this.duration = buf.readInt();
        this.bloom = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(xEnd);
        buf.writeDouble(yEnd);
        buf.writeDouble(zEnd);
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
        buf.writeInt(duration);
        buf.writeBoolean(bloom);
    }

    public static class MessageHandlerRenderRay implements IMessageHandler<MessageRenderRay, IMessage> {
        @Override
        @SuppressWarnings("deprecation")
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageRenderRay messageIn, MessageContext ctx) {
            final MessageRenderRay message = messageIn;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                RenderManager.getInstance().addRenderObjectToList(new Ray(message.x, message.y, message.z,
                                                                          message.xEnd, message.yEnd, message.zEnd,
                                                                          message.r, message.g, message.b,
                                                                          message.duration, message.bloom));
            });
            return null;
        }
    }
}
