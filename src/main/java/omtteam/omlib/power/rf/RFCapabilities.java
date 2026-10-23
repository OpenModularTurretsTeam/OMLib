package omtteam.omlib.power.rf;

import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class RFCapabilities {
    @CapabilityInject(IEnergyReceiver.class)
    public static Capability<IEnergyReceiver> CAPABILITY_CONSUMER = null;
}
