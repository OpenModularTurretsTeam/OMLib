package omtteam.omlib.client.platform;

//? if fabric {
import omtteam.omlib.client.platform.fabric.FabricClientNetwork;
//?}
//? if neoforge {
/*import omtteam.omlib.client.platform.neoforge.NeoForgeClientNetwork;
import omtteam.omlib.platform.Platform;
import omtteam.omlib.platform.neoforge.NeoForgeNetwork;
*///?}
//? if forge {
/*import omtteam.omlib.client.platform.forge.ForgeClientNetwork;
import omtteam.omlib.platform.Platform;
import omtteam.omlib.platform.forge.ForgeNetwork;
*///?}

/**
 * Holds the {@link ClientPlatformServices} of the running loader; the client counterpart of
 * {@link omtteam.omlib.platform.Platform}. The services are created on first use, whichever mod's client starts first.
 */
public final class ClientPlatform {
    private static ClientPlatformServices services;

    private ClientPlatform() {
    }

    /** Creates the services of the running loader unless that already happened. */
    public static synchronized void ensureInstalled() {
        if (services == null) {
            services = loaderServices();
        }
    }

    public static ClientNetwork network() {
        return services().network();
    }

    private static ClientPlatformServices services() {
        ensureInstalled();
        return services;
    }

    private static ClientPlatformServices loaderServices() {
        //? if fabric {
        return new ClientPlatformServices(new FabricClientNetwork());
        //?} else if neoforge {
        /*return new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network()));
        *///?} else {
        /*return new ClientPlatformServices(new ForgeClientNetwork((ForgeNetwork) Platform.network()));
        *///?}
    }
}
