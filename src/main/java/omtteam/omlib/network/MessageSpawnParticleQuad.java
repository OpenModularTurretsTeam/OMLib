package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

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

    public int getId() {
        return id;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getXRange() {
        return xRange;
    }

    public double getYRange() {
        return yRange;
    }

    public double getZRange() {
        return zRange;
    }

    public double getSpeedFactor() {
        return speedFactor;
    }

    public int getAmount() {
        return amount;
    }
}
