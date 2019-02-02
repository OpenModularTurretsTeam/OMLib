package omtteam.omlib.power.ic2;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.ILocatable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.tileentity.TileEntityElectric;

import static omtteam.omlib.compatibility.OMLibModCompatibility.IC2ModId;

/**
 * A Receiver for EU
 */

public class BaseOMEUReceiverWrapper implements IEnergySink, ILocatable {
    private final TileEntityElectric tile;
    private final EnumFacing facing;

    public BaseOMEUReceiverWrapper(TileEntityElectric tile, EnumFacing facing) {
        this.tile = tile;
        this.facing = facing;
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public double injectEnergy(EnumFacing facing, double v, double v1) {
        OMEnergyStorage storage = (OMEnergyStorage) tile.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null) {
            storage.receiveEnergy((int) (v * OMConfig.GENERAL.EUtoRFRatio), false);
            return 0.0D;
        }
        return v;
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public int getSinkTier() {
        return 4;
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public double getDemandedEnergy() {
        OMEnergyStorage storage = (OMEnergyStorage) tile.getCapability(CapabilityEnergy.ENERGY, facing);
        if (storage != null && OMConfig.GENERAL.EUSupport) {
            return (storage.getMaxEnergyStored() - storage.getEnergyStored()) / OMConfig.GENERAL.EUtoRFRatio;
        }
        return 0;
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing) {
        return true;
    }

    public World getWorldObj() {
        return tile.getWorld();
    }

    public BlockPos getPosition() {
        return tile.getPos();
    }
}