package omtteam.omlib.api.tile;

import omtteam.omlib.api.permission.IHasOwner;

public interface IHasTrustManager extends IHasOwner {
    TrustedPlayersManagerTile getTrustManager();
}
