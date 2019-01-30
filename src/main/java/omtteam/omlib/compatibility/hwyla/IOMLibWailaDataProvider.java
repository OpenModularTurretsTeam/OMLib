package omtteam.omlib.compatibility.hwyla;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public interface IOMLibWailaDataProvider extends IWailaDataProvider {
    void callbackRegister(IWailaRegistrar register);
}
