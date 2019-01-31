package omtteam.omlib.power.rf;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class RFCapabilities {
    @CapabilityInject(IEnergyReceiver.class)
    public static Capability<IEnergyReceiver> CAPABILITY_CONSUMER = null;
    @CapabilityInject(IEnergyProvider.class)
    public static Capability<IEnergyProvider> CAPABILITY_PRODUCER = null;
}
