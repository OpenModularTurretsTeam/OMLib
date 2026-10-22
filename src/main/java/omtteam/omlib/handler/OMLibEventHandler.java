package omtteam.omlib.handler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import omtteam.omlib.api.network.OMLibNetwork;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.network.MessageSetSharePlayerList;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.registry.OMLibItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Keridos on 17/05/17.
 * This Class is the EventHandler for OMLib.
 */
public class OMLibEventHandler {
    private static final ResourceLocation REMOVED_CABLE = new ResourceLocation(Reference.MOD_ID, "network_cable");

    private static OMLibEventHandler instance;
    private final List<OMLibNetwork> networks = new ArrayList<>();

    private OMLibEventHandler() {
    }

    public static OMLibEventHandler getInstance() {
        if (instance == null) {
            instance = new OMLibEventHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Reference.MOD_ID)) {
            ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public void playerJoinEvent(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerMP) {
            OMLibNetworkingHandler.sendMessageToPlayer(new MessageSetSharePlayerList(OwnerShareRegister.instance), (EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    public void itemRegisterEvent(RegistryEvent.Register<Item> event) {
        OMLibItems.init(event.getRegistry());
    }

    /** The network cable was removed in 4.0.0; worlds and inventories drop it silently. */
    @SubscribeEvent
    public void missingBlockMappings(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
            if (REMOVED_CABLE.equals(mapping.key)) mapping.ignore();
        }
    }

    @SubscribeEvent
    public void missingItemMappings(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (REMOVED_CABLE.equals(mapping.key)) mapping.ignore();
        }
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.WorldTickEvent event) {
        for (OMLibNetwork network : networks) {
            if (network.getWorld().provider.getDimension() == event.world.provider.getDimension()) {
                network.tick();
            }
        }
    }

    private List<OMLibNetwork> getNetworkList() {
        return networks;
    }

    public void registerNetwork(OMLibNetwork network) {
        networks.add(network);
    }

    public void removeNetwork(OMLibNetwork network) {
        networks.remove(network);
    }

    @Nullable
    public OMLibNetwork getNetwork(UUID uuid) {
        OMLibNetwork network = null;
        for (OMLibNetwork net : networks) {
            if (net.getUuid().equals(uuid)) {
                network = net;
                break;
            }
        }
        return network;
    }
}

