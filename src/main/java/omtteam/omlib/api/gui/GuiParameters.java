package omtteam.omlib.api.gui;

import net.minecraft.world.World;

public class GuiParameters {
    private Object mod;
    private int modGuiId;
    private World world;
    private int posX;
    private int posY;
    private int posZ;

    public GuiParameters(Object mod, int modGuiId, World world, int posX, int posY, int posZ) {
        this.mod = mod;
        this.modGuiId = modGuiId;
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public Object getMod() {
        return mod;
    }

    public int getModGuiId() {
        return modGuiId;
    }

    public World getWorld() {
        return world;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosZ() {
        return posZ;
    }
}
