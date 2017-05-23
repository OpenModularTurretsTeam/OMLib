package omtteam.omlib.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import omtteam.omlib.network.messages.MessageSetSharePlayerList;

/**
 * Created by Keridos on 17/05/17.
 * This Class is the EventHandler for OMLib
 */
public class EventHandler {
    private static EventHandler instance;

    private EventHandler() {
    }

    public static EventHandler getInstance() {
        if (instance == null) {
            instance = new EventHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public void worldLoadEvent(WorldEvent.Load event) {
        OwnerShareHandler.loadFromDisk();
    }

    @SubscribeEvent
    public void worldUnloadEvent(WorldEvent.Unload event) {
        OwnerShareHandler.saveToDisk();
    }

    @SubscribeEvent
    public void playerJoinEvent(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP) {
            OMLibNetworkingHandler.sendMessageToPlayer(new MessageSetSharePlayerList(OwnerShareHandler.getInstance()), (EntityPlayerMP) event.getEntity());
        }
    }
}
