package omtteam.omlib.api.render.object;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class RenderObject {
    protected double x, y, z;
    protected int duration;

    RenderObject(double x, double y, double z, int duration) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.duration = duration;
    }

    public boolean decreaseDuration() {
        duration--;
        boolean ret = duration <= 0;
        return ret;
    }
    public abstract void render(RenderGlobal renderGlobal, float partialTick);
}
