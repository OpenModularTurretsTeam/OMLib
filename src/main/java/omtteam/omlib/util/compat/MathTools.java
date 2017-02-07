package omtteam.omlib.util.compat;

import net.minecraft.util.math.MathHelper;

/**
 * Created by Keridos on 07/02/17.
 * This Class
 */
public class MathTools extends MathHelper {
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

    public static double abs_max(double p_76132_0_, double p_76132_2_) {
        return MathHelper.abs_max(p_76132_0_, p_76132_2_);
    }

    public static float sqrt_double(double p_76132_0_) {
        return MathHelper.sqrt_double(p_76132_0_);
    }

    public static int floor_double(double p_76132_0_) {
        return floor(p_76132_0_);
    }
}