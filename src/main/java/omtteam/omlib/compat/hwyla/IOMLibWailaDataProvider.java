package omtteam.omlib.compat.hwyla;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public interface IOMLibWailaDataProvider extends IWailaDataProvider {
    void callbackRegister(IWailaRegistrar register);
}
