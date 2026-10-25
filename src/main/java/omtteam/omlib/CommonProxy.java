package omtteam.omlib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.config.ConfigSync;
import omtteam.omlib.handler.OMLibEventHandler;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.util.RandomUtil;

import javax.annotation.Nullable;

@SuppressWarnings({"EmptyMethod", "WeakerAccess", "unused"})
public class CommonProxy {
    public void preInit() {
        initHandlers();
        OwnerShareRegister.preInit();
        RandomUtil.init();
    }

    public void initHandlers() {
        OMLibNetworkingHandler.initNetworking();
        ConfigSync.register(Reference.MOD_ID);
        MinecraftForge.EVENT_BUS.register(OMLibEventHandler.getInstance());
    }

    public void init() {

    }

    /** The handler of a server-to-client message: the client proxy supplies the one in client.network, a server none. */
    public <M extends IMessage> IMessageHandler<M, IMessage> clientMessageHandler(Class<M> type) {
        return (message, ctx) -> null;
    }

    /** The GUI screen for {@code OMGuiHandler}; screens only exist on the client. */
    @Nullable
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}
