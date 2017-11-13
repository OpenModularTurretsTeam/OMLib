package omtteam.omlib.proxy;

import net.minecraftforge.common.MinecraftForge;
import omtteam.omlib.handler.EventHandler;
import omtteam.omlib.handler.OMLibNetworkingHandler;

@SuppressWarnings({"EmptyMethod", "WeakerAccess", "unused"})
public class CommonProxy {
    public void preInit() {
        initHandlers();
    }

    public void initRenderers() {

    }

    public void initHandlers() {
        OMLibNetworkingHandler.initNetworking();
        MinecraftForge.EVENT_BUS.register(EventHandler.getInstance());
    }

    public void init() {

    }
}