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
import omtteam.omlib.util.RandomUtil;
import omtteam.omlib.util.command.CommandChangeOwner;
import omtteam.omlib.util.command.CommandShareOwner;
import omtteam.omlib.util.command.CommandToggleDebug;
import org.apache.logging.log4j.Logger;

import static omtteam.omlib.compatability.ModCompatibility.checkForMods;
import static omtteam.omlib.compatability.ModCompatibility.performModCompat;


@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, acceptedMinecraftVersions = "1.11.2", dependencies = Reference.DEPENDENCIES)
public class OMLib {
    @SuppressWarnings("unused")
    @Mod.Instance(Reference.MOD_ID)
    public static OMLib instance;

    @SuppressWarnings("unused")
    @SidedProxy(clientSide = "omtteam.omlib.proxy.ClientProxy", serverSide = "omtteam.omlib.proxy" + "" + ".CommonProxy")
    private static CommonProxy proxy;

    @SuppressWarnings("unused")
    public static CreativeTabs modularTurretsTab;
    private static Logger logger;

    public static Logger getLogger() {
        return logger;
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ConfigHandler.init(event.getSuggestedConfigurationFile());
        checkForMods();
        RandomUtil.init();
        proxy.preInit();
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        performModCompat();
        proxy.init();
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandChangeOwner());
        event.registerServerCommand(new CommandShareOwner());
        event.registerServerCommand(new CommandToggleDebug());
    }
}