package omtteam.omlib.api.render;

import java.awt.*;

public class ColorOM {
    public float r, g, b, a;

    public ColorOM(float r, float g, float b, float a) {
        this.r = Math.min(r, 1F);
        this.g = Math.min(g, 1F);
        ;
        this.b = Math.min(b, 1F);
        ;
        this.a = Math.min(a, 1F);
        ;
    }

    public ColorOM(Color color) {
        this.r = Math.min(color.getRed() / 255F, 1F);
        this.g = Math.min(color.getGreen() / 255F, 1F);
        ;
        this.b = Math.min(color.getBlue() / 255F, 1F);
        ;
        this.a = Math.min(color.getAlpha() / 255F, 1F);
        ;
    }

    public int getRInt() {
        return Math.round(r * 255F);
    }

    public int getGInt() {
        return Math.round(g * 255F);
    }

    public int getBInt() {
        return Math.round(b * 255F);
    }

    public int getAlphaInt() {
        return Math.round(a * 255F);
    }
}
