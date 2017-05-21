package omtteam.omlib.handler;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

import static omtteam.omlib.util.OwnerShareDiskIO.loadFromDisk;
import static omtteam.omlib.util.OwnerShareDiskIO.saveToDisk;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public class EventHandler {

    @SubscribeEvent
    public void worldLoadEvent(WorldEvent.Load event){
        OwnerShareHandler.getInstance().setOwnerShareMap(loadFromDisk());
        if (OwnerShareHandler.getInstance().getOwnerShareMap() == null){
            OwnerShareHandler.getInstance().setOwnerShareMap(new HashMap<>());
        }
    }

    @SubscribeEvent
    public void worldUnloadEvent(WorldEvent.Unload event){
        saveToDisk(OwnerShareHandler.getInstance());
    }
}
