package omtteam.omlib.proxy;

import net.minecraftforge.common.MinecraftForge;
import omtteam.omlib.api.permission.GlobalTrustRegister;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.handler.OMLibEventHandler;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.util.RandomUtil;

@SuppressWarnings({"EmptyMethod", "WeakerAccess", "unused"})
public class CommonProxy {
    public void preInit() {
        initHandlers();
        OwnerShareRegister.preInit();
        GlobalTrustRegister.preInit();
        RandomUtil.init();
    }

    public void initRenderers() {

    }

    public void initHandlers() {
        OMLibNetworkingHandler.initNetworking();
        MinecraftForge.EVENT_BUS.register(OMLibEventHandler.getInstance());
    }

    public void init() {

    }
}