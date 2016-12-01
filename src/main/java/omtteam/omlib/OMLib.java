package omtteam.omlib;


import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.proxy.CommonProxy;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.util.CommandChangeOwner;

import static omtteam.omlib.compatability.ModCompatibility.checkForMods;
import static omtteam.omlib.compatability.ModCompatibility.performModCompat;


@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, acceptedMinecraftVersions = "1.7.10", dependencies = Reference.DEPENDENCIES)
public class OMLib {
    @Mod.Instance(Reference.MOD_ID)
    public static OMLib instance;

    @SidedProxy(clientSide = "omtteam.omlib.proxy.ClientProxy", serverSide = "omtteam.omlib.proxy" + "" + ".CommonProxy")
    private static CommonProxy proxy;

    public static CreativeTabs modularTurretsTab;


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());
        checkForMods();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        performModCompat();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandChangeOwner());
    }
}