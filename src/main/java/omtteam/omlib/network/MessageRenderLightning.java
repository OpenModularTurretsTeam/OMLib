package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

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

    public Vec3d getStart() {
        return new Vec3d(x, y, z);
    }

    public Vec3d getEnd() {
        return new Vec3d(xEnd, yEnd, zEnd);
    }

    public int getDuration() {
        return duration;
    }
}
