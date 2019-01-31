package omtteam.omlib.power.ic2;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class EUCapabilities {
    @CapabilityInject(IEnergySink.class)
    public static Capability<IEnergySink> CAPABILITY_CONSUMER = null;
    @CapabilityInject(IEnergyEmitter.class)
    public static Capability<IEnergyEmitter> CAPABILITY_PRODUCER = null;
}
