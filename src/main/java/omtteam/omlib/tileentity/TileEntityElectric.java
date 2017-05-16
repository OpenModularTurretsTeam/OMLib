package omtteam.omlib.tileentity;

import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import mcp.MethodsReturnNonnullByDefault;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.compatability.ModCompatibility;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.power.tesla.BaseOMTeslaContainerWrapper;
import omtteam.omlib.util.MathUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.compatability.ModCompatibility.IC2Loaded;
import static omtteam.omlib.compatability.ModCompatibility.IC2ModId;
import static omtteam.omlib.compatability.ModCompatibility.TeslaModId;
import static omtteam.omlib.handler.ConfigHandler.EUSupport;

/**
 * Created by Keridos on 27/04/17.
 * This Class
 */

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
@Optional.InterfaceList({
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = IC2ModId),
        @Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = "CoFHAPI")})
@MethodsReturnNonnullByDefault
public abstract class TileEntityElectric extends TileEntityOwnedBlock implements IEnergyReceiver, ITickable, IEnergySink {
    protected OMEnergyStorage storage;
    protected Object teslaContainer;
    protected double storageEU = 0D;
    //private float amountOfPotentia = 0F;
    //private final float maxAmountOfPotentia = ConfigHandler.getPotentiaAddonCapacity();

    protected boolean wasAddedToEnergyNet = false;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        nbtTagCompound.setInteger("maxStorage", this.storage.getMaxEnergyStored());
        nbtTagCompound.setInteger("energyStored", this.storage.getEnergyStored());
        nbtTagCompound.setInteger("maxIO", this.storage.getMaxReceive());
        nbtTagCompound.setDouble("storageEU", storageEU);

        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.storage.setCapacity(nbtTagCompound.getInteger("maxStorage"));
        this.storage.setEnergyStored(nbtTagCompound.getInteger("energyStored"));
        this.storage.setMaxReceive(nbtTagCompound.getInteger("maxIO"));
        this.storageEU = nbtTagCompound.getDouble("storageEU");

    }

    @Override
    public void update() {
        if (IC2Loaded && EUSupport && !wasAddedToEnergyNet && !this.getWorld().isRemote) {
            addToIc2EnergyNetwork();
            wasAddedToEnergyNet = true;
        }
        if (!this.getWorld().isRemote && IC2Loaded && this.getWorld().getWorldTime() % 20 == 1) {
            moveEnergyFromIC2ToStorage();
        }
    }

    /*
    @Optional.Method(modid = "Thaumcraft")
    private IEssentiaTransport getConnectableTileWithoutOrientation() {
        if (worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord) instanceof IEssentiaTransport) {
            return (IEssentiaTransport) worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord);
        }

        if (worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord) instanceof IEssentiaTransport) {
            return (IEssentiaTransport) worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord);
        }

        if (worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord) instanceof IEssentiaTransport) {
            return (IEssentiaTransport) worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
        }

        if (worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof IEssentiaTransport) {
            return (IEssentiaTransport) worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
        }

        if (worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1) instanceof IEssentiaTransport) {
            return (IEssentiaTransport) worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1);
        }

        if (worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1) instanceof IEssentiaTransport) {
            return (IEssentiaTransport) worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1);
        }
        return null;
    }

    @Optional.Method(modid = "Thaumcraft")
    private int drawEssentia() {
        IEssentiaTransport ic = getConnectableTileWithoutOrientation();
        if (ic != null) {
            if (ic.takeEssentia(Aspect.ENERGY, 1, caForgeDirection.UP) == 1) {
                return 1;
            }
        }
        return 0;
    }*/

    @Optional.Method(modid = "CoFHAPI")
    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        this.markDirty();
        return storage.receiveEnergy(maxReceive, simulate);
    }

    @Optional.Method(modid = "CoFHAPI")
    @Override
    public int getEnergyStored(EnumFacing from) {
        return storage.getEnergyStored();
    }

    @Optional.Method(modid = "CoFHAPI")
    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return storage.getMaxEnergyStored();
    }

    @Optional.Method(modid = "CoFHAPI")
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    public int getEnergyLevel(EnumFacing from) {
        return storage.getEnergyStored();
    }

    public int getMaxEnergyLevel(EnumFacing from) {
        return storage.getMaxEnergyStored();
    }

    public void setMaxEnergyStored(int maxStorage) {
        storage.setCapacity(maxStorage);
    }

    public void setEnergyStored(int energy) {
        storage.setEnergyStored(energy);
        this.markDirty();
    }

    public void moveEnergyFromIC2ToStorage() {
        double requiredEnergy = (storage.getMaxEnergyStored() - storage.getEnergyStored()) / ConfigHandler.EUtoRFRatio;
        if (storageEU >= requiredEnergy) {
            storageEU -= requiredEnergy;
            storage.modifyEnergyStored(MathUtil.truncateDoubleToInt((requiredEnergy * ConfigHandler.EUtoRFRatio)));
        } else {
            storage.modifyEnergyStored(MathUtil.truncateDoubleToInt(storageEU * ConfigHandler.EUtoRFRatio));
            storageEU = 0D;
        }
        this.markDirty();
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public double injectEnergy(EnumFacing facing, double v, double v1) {
        storageEU += v;
        return 0.0D;
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public int getSinkTier() {
        return 4;
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public double getDemandedEnergy() {
        if (ConfigHandler.EUSupport) {
            return Math.max(4000D - storageEU, 0.0D);
        }
        return 0;
    }

    @Optional.Method(modid = IC2ModId)
    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter tileEntity, EnumFacing facing) {
        return true;
    }

    @Optional.Method(modid = IC2ModId)
    protected void addToIc2EnergyNetwork() {
        if (!world.isRemote) {
            EnergyTileLoadEvent event = new EnergyTileLoadEvent(this);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Optional.Method(modid = IC2ModId)
    private void removeFromIc2EnergyNetwork() {
        MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        onChunkUnload();
    }

    @Override
    public void onChunkUnload() {
        if (wasAddedToEnergyNet &&
                ModCompatibility.IC2Loaded) {
            removeFromIc2EnergyNetwork();

            wasAddedToEnergyNet = false;
        }
    }


    @Optional.Method(modid = TeslaModId)
    private BaseOMTeslaContainerWrapper getTeslaContainer() {
        if (teslaContainer instanceof BaseOMTeslaContainerWrapper) {
            return (BaseOMTeslaContainerWrapper) teslaContainer;
        } else {
            teslaContainer = new BaseOMTeslaContainerWrapper(this, EnumFacing.DOWN);
            return (BaseOMTeslaContainerWrapper) teslaContainer;
        }

    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    @ParametersAreNonnullByDefault
    @Nullable
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

        // This method is where other things will try to access your TileEntity's Tesla
        // capability. In the case of the analyzer, is a consumer, producer and holder so we
        // can allow requests that are looking for any of those things. This example also does
        // not care about which side is being accessed, however if you wanted to restrict which
        // side can be used, for example only allow power input through the back, that could be
        // done here.
        if (ModCompatibility.TeslaLoaded) {
            if (hasTeslaCapability(capability,facing) && getTeslaCapability(capability, facing) != null) {
                return getTeslaCapability(capability, facing);
            }
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) storage;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {

        // This method replaces the instanceof checks that would be used in an interface based
        // system. It can be used by other things to see if the TileEntity uses a capability or
        // not. This example is a Consumer, Producer and Holder, so we return true for all
        // three. This can also be used to restrict access on certain sides, for example if you
        // only accept power input from the bottom of the block, you would only return true for
        // Consumer if the facing parameter was down.
        if (ModCompatibility.TeslaLoaded) {
            if (hasTeslaCapability(capability, facing)) {
                return true;
            }
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unused")
    @Optional.Method(modid = TeslaModId)
    private boolean hasTeslaCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == TeslaCapabilities.CAPABILITY_CONSUMER);
    }

    @Optional.Method(modid = TeslaModId)
    @SuppressWarnings({"unchecked", "unused"})
    @Nullable
    private <T> T getTeslaCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == TeslaCapabilities.CAPABILITY_CONSUMER) {
            return (T) getTeslaContainer();
        }
        return null;
    }

    /*
    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean isConnectable(ForgeDirection face) {
        return TurretHeadUtil.hasPotentiaUpgradeAddon(this);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean canInputFrom(ForgeDirection face) {
        return TurretHeadUtil.hasPotentiaUpgradeAddon(this);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean canOutputTo(ForgeDirection face) {
        return false;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public Aspect getSuctionType(ForgeDirection face) {
        return null;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return 0;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public Aspect getEssentiaType(ForgeDirection face) {
        return null;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int getEssentiaAmount(ForgeDirection face) {
        return 0;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public AspectList getAspects() {
        if (TurretHeadUtil.hasPotentiaUpgradeAddon(this)) {
            return new AspectList().add(Aspect.ENERGY, (int) Math.floor(amountOfPotentia));
        } else {
            return null;
        }
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public void setAspects(AspectList aspects) {
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return tag.equals(Aspect.ENERGY);
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int getSuctionAmount(ForgeDirection face) {
        return 64;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
        return 0;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Optional.Method(modid = "Thaumcraft")
    @Override
    public int containerContains(Aspect tag) {
        if (tag.equals(Aspect.ENERGY)) {
            return Math.round(amountOfPotentia);
        }
        return 0;
    } */
}
