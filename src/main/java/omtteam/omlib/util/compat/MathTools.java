package omtteam.omlib.util.compat;

import net.minecraft.util.math.MathHelper;

/**
 * Created by Keridos on 07/02/17.
 * This Class
 */
public class MathTools {
    public MathTools() {
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int ceiling(float value) {
        int i = (int) value;
        return value > i ? i + 1 : i;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static int clamp(int num, int min, int max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static double clamp(double lowerBnd, double upperBnd, double slide) {
        return slide < 0.0D ? lowerBnd : (slide > 1.0D ? upperBnd : lowerBnd + (upperBnd - lowerBnd) * slide);
    }

    public static double abs_max(double max1, double max2) {
        return MathHelper.absMax(max1, max2);
    }

    public static float sqrt_double(double sqrt) {
        return MathHelper.sqrt(sqrt);
    }

    public static int floor_double(double floor) {
        return floor(floor);
    }
}