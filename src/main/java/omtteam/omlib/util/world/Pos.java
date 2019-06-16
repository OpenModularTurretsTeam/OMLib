package omtteam.omlib.util.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class Pos {
    private double x, y, z;

    public Pos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Pos(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double distance(Entity entity) {
        return Math.cbrt(Math.pow(x - entity.posX, 2) + Math.pow(y - entity.posY, 2) + Math.pow(z - entity.posZ, 2));
    }

    public double distance(BlockPos pos) {
        return Math.cbrt(Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2) + Math.pow(z - pos.getZ(), 2));
    }
}
