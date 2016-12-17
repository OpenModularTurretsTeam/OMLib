package omtteam.omlib.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 01/10/2015.
 * This Class
 */
@SuppressWarnings({"unused", "SuspiciousNameCombination"})
public class MathUtil {
    public static int truncateDoubleToInt(double number) {
        return (int) Math.floor(number);
    }

    @ParametersAreNonnullByDefault
    public static AxisAlignedBB rotateAABB(AxisAlignedBB box, EnumFacing facing){
        switch (facing) {
            case NORTH:
                return box.offset(0F,0F,0.625F);
            case EAST:
                return new AxisAlignedBB(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX);
            case SOUTH:
                return box;
            case WEST:
                return new AxisAlignedBB(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX).offset(0.625F,0F,0F);
            case UP:
                return new AxisAlignedBB(box.minY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX);
            case DOWN:
                return new AxisAlignedBB(box.minY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX).offset(0F,0.625F,0F);
        }
        return box;
    }
}
