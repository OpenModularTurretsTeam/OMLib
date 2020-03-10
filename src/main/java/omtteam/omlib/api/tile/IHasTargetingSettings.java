package omtteam.omlib.api.tile;

import omtteam.omlib.api.network.ISyncable;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.util.TargetingSettings;

public interface IHasTargetingSettings extends IHasOwner, ISyncable {
    TargetingSettings getTargetingSettings();
}
