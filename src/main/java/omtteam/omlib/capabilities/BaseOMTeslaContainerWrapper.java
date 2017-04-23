package omtteam.omlib.capabilities;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import omtteam.omlib.tileentity.TileEntityMachine;

/**
 * A Receiver for Energy, credits go to the ender IO devs :)
 */
@SuppressWarnings("WeakerAccess")
public class BaseOMTeslaContainerWrapper implements ITeslaConsumer {
    public static class RecieverTileCapabilityProvider implements ICapabilityProvider {

        private final TileEntityMachine tile;

        public RecieverTileCapabilityProvider(TileEntityMachine tile) {
            this.tile = tile;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == TeslaCapabilities.CAPABILITY_CONSUMER) {
                return (T) new BaseOMTeslaContainerWrapper(tile, facing);
            }
            return null;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == TeslaCapabilities.CAPABILITY_CONSUMER;
        }

    }

    private final TileEntityMachine tile;
    private final EnumFacing facing;

    public BaseOMTeslaContainerWrapper(TileEntityMachine tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    @Override
    public long givePower(long power, boolean simulated) {
        return tile.receiveEnergy(facing, (int) power, simulated);
    }

}