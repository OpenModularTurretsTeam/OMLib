package omtteam.omlib.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import omtteam.omlib.util.compat.MinecraftTools;

@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused"})
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void initRenderers() {
        super.initRenderers();
    }

    @Override
    public void initHandlers() {
        super.initHandlers();
    }

    public static World getWorld(Minecraft mc) {
        return MinecraftTools.getWorld(mc);
    }
}