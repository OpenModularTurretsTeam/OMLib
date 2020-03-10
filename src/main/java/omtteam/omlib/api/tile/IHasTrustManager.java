package omtteam.omlib.api.tile;

import omtteam.omlib.api.network.ISyncable;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.api.permission.TrustedPlayersManager;

public interface IHasTrustManager extends IHasOwner, ISyncable {
    TrustedPlayersManager getTrustManager();
}
