package omtteam.omlib.power.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */

public class BaseOMTeslaReceiverWrapper implements ITeslaConsumer {

    private final IEnergyStorage storage;

    public BaseOMTeslaReceiverWrapper(IEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public long givePower(long power, boolean simulated) {
        return storage.receiveEnergy((int) Math.min(power, Integer.MAX_VALUE), simulated);
    }
}
