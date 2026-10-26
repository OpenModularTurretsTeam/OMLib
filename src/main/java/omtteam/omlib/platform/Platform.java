package omtteam.omlib.platform;

//? if fabric {
import omtteam.omlib.platform.fabric.FabricEnergy;
import omtteam.omlib.platform.fabric.FabricFuel;
import omtteam.omlib.platform.fabric.FabricLoaderInfo;
import omtteam.omlib.platform.fabric.FabricMenus;
import omtteam.omlib.platform.fabric.FabricNetwork;
//?}
//? if neoforge {
/*import omtteam.omlib.platform.neoforge.NeoForgeEnergy;
import omtteam.omlib.platform.neoforge.NeoForgeFuel;
import omtteam.omlib.platform.neoforge.NeoForgeLoaderInfo;
import omtteam.omlib.platform.neoforge.NeoForgeMenus;
import omtteam.omlib.platform.neoforge.NeoForgeNetwork;
*///?}
//? if forge {
/*import omtteam.omlib.platform.forge.ForgeEnergy;
import omtteam.omlib.platform.forge.ForgeFuel;
import omtteam.omlib.platform.forge.ForgeLoaderInfo;
import omtteam.omlib.platform.forge.ForgeMenus;
import omtteam.omlib.platform.forge.ForgeNetwork;
*///?}

/**
 * Holds the {@link PlatformServices} of the running loader, shared by all mods that use the library. The services are
 * created on first use (or by the library's entrypoint, {@link #ensureInstalled()}), so it does not matter which mod
 * starts first; shared code reaches each service through the static getters.
 */
public final class Platform {
    private static PlatformServices services;

    private Platform() {
    }

    /** Creates the services of the running loader unless that already happened. */
    public static synchronized void ensureInstalled() {
        if (services == null) {
            services = loaderServices();
        }
    }

    public static LoaderInfo loaderInfo() {
        return services().loaderInfo();
    }

    public static Fuel fuel() {
        return services().fuel();
    }

    public static Network network() {
        return services().network();
    }

    public static Menus menus() {
        return services().menus();
    }

    public static Energy energy() {
        return services().energy();
    }

    private static PlatformServices services() {
        ensureInstalled();
        return services;
    }

    private static PlatformServices loaderServices() {
        //? if fabric {
        return new PlatformServices(new FabricLoaderInfo(), new FabricFuel(), new FabricNetwork(), new FabricMenus(),
                new FabricEnergy());
        //?} else if neoforge {
        /*return new PlatformServices(new NeoForgeLoaderInfo(), new NeoForgeFuel(), new NeoForgeNetwork(),
                new NeoForgeMenus(), new NeoForgeEnergy());
        *///?} else {
        /*return new PlatformServices(new ForgeLoaderInfo(), new ForgeFuel(), new ForgeNetwork(), new ForgeMenus(),
                new ForgeEnergy());
        *///?}
    }
}
