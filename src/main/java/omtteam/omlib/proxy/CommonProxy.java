package omtteam.omlib.proxy;

import net.minecraftforge.common.MinecraftForge;
import omtteam.omlib.handler.EventHandler;
import omtteam.omlib.handler.OMLibNetworkingHandler;
import omtteam.omlib.init.OMLibItems;

@SuppressWarnings({"EmptyMethod", "WeakerAccess", "unused"})
public class CommonProxy {
    public void preInit() {
        OMLibItems.init();
        initHandlers();
    }

    public void initRenderers() {

    }

    public void initHandlers() {
        OMLibNetworkingHandler.initNetworking();
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(EventHandler.getInstance());
    }
}