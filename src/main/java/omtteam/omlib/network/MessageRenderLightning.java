package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import omtteam.omlib.api.render.ColorOM;

/**
 * Created by nico on 23/05/17.
 * This gives the client a Lightning object to be rendered in the next RenderWorldLastEvent.
 */
public class MessageRenderLightning implements IMessage {
    /** The colour lightning had before it could be chosen. */
    public static final ColorOM DEFAULT_COLOR = new ColorOM(0.6F, 1F, 1F, 0.8F);

    private double x, y, z, xEnd, yEnd, zEnd;
    private float r, g, b, a;
    private int duration;

    public MessageRenderLightning() {
    }

    /** A lightning in the former fixed cyan ({@link #DEFAULT_COLOR}). */
    public MessageRenderLightning(Vec3d start, Vec3d end, int duration) {
        this(start, end, DEFAULT_COLOR, duration);
    }

    public MessageRenderLightning(Vec3d start, Vec3d end, ColorOM color, int duration) {
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
}
