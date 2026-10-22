package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import omtteam.omlib.api.render.ColorOM;

/**
 * Created by nico on 23/05/17.
 * This gives the client a Ray object to be rendered in the next RenderWorldLastEvent.
 */
public class MessageRenderRay implements IMessage {
    private double x, y, z, xEnd, yEnd, zEnd;
    private float r, g, b, a;
    private int duration;
    private boolean bloom;

    public MessageRenderRay() {
    }

    public MessageRenderRay(double x, double y, double z, double xEnd, double yEnd, double zEnd, ColorOM color, int duration, boolean bloom) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.zEnd = zEnd;
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.duration = duration;
        this.bloom = bloom;
    }

    public MessageRenderRay(Vec3d start, Vec3d end, ColorOM color, int duration, boolean bloom) {
        this.x = start.x;
        this.y = start.y;
        this.z = start.z;
        this.xEnd = end.x;
        this.yEnd = end.y;
        this.zEnd = end.z;
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
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
        this.a = buf.readFloat();
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
        buf.writeFloat(a);
        buf.writeInt(duration);
        buf.writeBoolean(bloom);
    }

    public Vec3d getStart() {
        return new Vec3d(x, y, z);
    }

    public Vec3d getEnd() {
        return new Vec3d(xEnd, yEnd, zEnd);
    }

    public ColorOM getColor() {
        return new ColorOM(r, g, b, a);
    }

    public int getDuration() {
        return duration;
    }

    public boolean isBloom() {
        return bloom;
    }
}
