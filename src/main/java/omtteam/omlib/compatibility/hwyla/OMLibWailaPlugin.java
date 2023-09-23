package omtteam.omlib.compatibility.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keridos on 15/11/17.
 * This Class acts as the Waila/Hwyla plugin for all open modular mods.
 */

@WailaPlugin
public class OMLibWailaPlugin implements IWailaPlugin {
    private static final List<IOMLibWailaDataProvider> providers = new ArrayList<>();

    public static void addDataProvider(IOMLibWailaDataProvider provider) {
        providers.add(provider);
    }

    @Override
    public void register(IWailaRegistrar registrar) {
        for (IOMLibWailaDataProvider provider : providers) {
            provider.callbackRegister(registrar);
        }
    }
}
