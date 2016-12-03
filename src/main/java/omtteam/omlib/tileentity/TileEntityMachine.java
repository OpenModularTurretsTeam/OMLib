package omtteam.omlib.tileentity;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.capabilities.BaseOMTeslaContainer;
import omtteam.omlib.compatability.ModCompatibility;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.MathUtil;
import omtteam.omlib.util.TrustedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static omtteam.omlib.compatability.ModCompatibility.TeslaLoaded;
import static omtteam.omlib.handler.ConfigHandler.EUSupport;
import static omtteam.omlib.util.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.PlayerUtil.getPlayerUUID;

@Optional.InterfaceList({
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")})

public abstract class TileEntityMachine extends TileEntityContainer implements IEnergyReceiver, IEnergySink {

    protected EnergyStorage storage;
    protected Object teslaContainer;
    protected double storageEU;
    protected boolean active;
    protected boolean inverted;
    protected boolean redstone;
    protected boolean wasAddedToEnergyNet = false;
    protected List<TrustedPlayer> trustedPlayers;
    //private float amountOfPotentia = 0F;
    //private final float maxAmountOfPotentia = ConfigHandler.getPotentiaAddonCapacity();

    public TileEntityMachine() {
        super();
        this.trustedPlayers = new ArrayList<>();
        this.inventory = new ItemStack[13];
        this.storage = new EnergyStorage(10, 10);
        this.inverted = true;
        this.active = true;
    }

    public boolean addTrustedPlayer(String name) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(name);
        trustedPlayer.uuid = getPlayerUUID(name);
        if (trustedPlayer.uuid != null) {
            for (TrustedPlayer player : trustedPlayers) {
                if (player.getName().toLowerCase().equals(name.toLowerCase()) || trustedPlayer.uuid.toString().equals(
                        owner)) {
                    return true;
                }
            }
            trustedPlayers.add(trustedPlayer);
            return true;
        }
        return false;
    }

    public boolean removeTrustedPlayer(String name) {
        for (TrustedPlayer player : trustedPlayers) {
            if (player.getName().equals(name)) {
                trustedPlayers.remove(player);
                return true;
            }
        }
        return false;
    }

    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    public TrustedPlayer getTrustedPlayer(String name) {
        for (TrustedPlayer trustedPlayer : trustedPlayers) {
            if (trustedPlayer.name.equals(name)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    public TrustedPlayer getTrustedPlayer(UUID uuid) {
        for (TrustedPlayer trustedPlayer : trustedPlayers) {
            if (trustedPlayer.uuid.equals(uuid)) {
                return trustedPlayer;
            }
        }
        return null;
    }

    public void setTrustedPlayers(List<TrustedPlayer> list) {
        this.trustedPlayers = list;
    }

    private NBTTagList getTrustedPlayersAsNBT() {
        NBTTagList nbt = new NBTTagList();
        for (TrustedPlayer trustedPlayer : trustedPlayers) {
            NBTTagCompound nbtPlayer = new NBTTagCompound();
            nbtPlayer.setString("name", trustedPlayer.name);
            nbtPlayer.setBoolean("canOpenGUI", trustedPlayer.canOpenGUI);
            nbtPlayer.setBoolean("canChangeTargeting", trustedPlayer.canChangeTargeting);
            nbtPlayer.setBoolean("admin", trustedPlayer.admin);
            if (trustedPlayer.uuid != null) {
                nbtPlayer.setString("UUID", trustedPlayer.uuid.toString());
            } else if (getPlayerUUID(trustedPlayer.name) != null) {
                nbtPlayer.setString("UUID", getPlayerUUID(trustedPlayer.name).toString());
            }
            nbt.appendTag(nbtPlayer);
        }
        return nbt;
    }

    private void buildTrustedPlayersFromNBT(NBTTagList nbt) {
        trustedPlayers.clear();
        for (int i = 0; i < nbt.tagCount(); i++) {
            if (!nbt.getCompoundTagAt(i).getString("name").equals("")) {
                NBTTagCompound nbtPlayer = nbt.getCompoundTagAt(i);
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbtPlayer.getString("name"));
                trustedPlayer.canOpenGUI = nbtPlayer.getBoolean("canOpenGUI");
                trustedPlayer.canChangeTargeting = nbtPlayer.getBoolean("canChangeTargeting");
                trustedPlayer.admin = nbtPlayer.getBoolean("admin");
                if (nbtPlayer.hasKey("UUID")) {
                    trustedPlayer.uuid = getPlayerUIDUnstable(nbtPlayer.getString("UUID"));
                } else {
                    trustedPlayer.uuid = getPlayerUUID(trustedPlayer.name);
                }
                if (trustedPlayer.uuid != null) {
                    trustedPlayers.add(trustedPlayer);
                }
            } else if (nbt.getCompoundTagAt(i).getString("name").equals("")) {
                TrustedPlayer trustedPlayer = new TrustedPlayer(nbt.getStringTagAt(i));
                Logger.getGlobal().info("found legacy trusted Player: " + nbt.getStringTagAt(i));
                trustedPlayer.uuid = getPlayerUUID(trustedPlayer.name);
                if (trustedPlayer.uuid != null) {
                    trustedPlayers.add(trustedPlayer);
                }
            }
        }
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        nbtTagCompound.setInteger("maxStorage", this.storage.getMaxEnergyStored());
        nbtTagCompound.setInteger("energyStored", this.getEnergyStored(EnumFacing.DOWN));
        nbtTagCompound.setInteger("maxIO", this.storage.getMaxReceive());
        nbtTagCompound.setTag("trustedPlayers", getTrustedPlayersAsNBT());
        nbtTagCompound.setDouble("storageEU", storageEU);
        nbtTagCompound.setBoolean("active", active);
        nbtTagCompound.setBoolean("inverted", inverted);
        nbtTagCompound.setBoolean("redstone", redstone);

        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.storage.setCapacity(nbtTagCompound.getInteger("maxStorage"));
        this.storage.setEnergyStored(nbtTagCompound.getInteger("energyStored"));
        this.storage.setMaxReceive(nbtTagCompound.getInteger("maxIO"));
        this.active = !nbtTagCompound.hasKey("active") || nbtTagCompound.getBoolean("active");
        this.inverted = !nbtTagCompound.hasKey("inverted") || nbtTagCompound.getBoolean("inverted");
        if (nbtTagCompound.hasKey("redstone")) {
            this.redstone = nbtTagCompound.getBoolean("redstone");
        }
        buildTrustedPlayersFromNBT(nbtTagCompound.getTagList("trustedPlayers", 10));
        if (nbtTagCompound.hasKey("storageEU")) {
            this.storageEU = nbtTagCompound.getDouble("storageEU");
        } else {
            storageEU = 0;
        }
    }

    public boolean isActive() {
        return active;
    }

    protected boolean getInverted() {
        return this.inverted;
    }

    protected void setInverted(boolean inverted) {
        this.inverted = inverted;
        this.active = redstone ^ this.inverted;
        this.markDirty();
    }

    protected boolean getRedstone() {
        return this.redstone;
    }

    public void setRedstone(boolean redstone) {
        this.redstone = redstone;
        this.active = this.redstone ^ inverted;
        this.markDirty();
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
            if (ic.takeEssentia(Aspect.ENERGY, 1, ForgeDirection.UP) == 1) {
                return 1;
            }
        }
        return 0;
    }*/


    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        this.markDirty();
        return storage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return storage.getMaxEnergyStored();
    }

    public void setEnergyStored(int energy) {
        storage.setEnergyStored(energy);
        this.markDirty();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public double injectEnergy(EnumFacing facing, double v, double v1) {
        storageEU += v;
        this.markDirty();
        return 0.0D;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public int getSinkTier() {
        return 4;
    }

    @Optional.Method(modid = "IC2")
    @Override
    public double getDemandedEnergy() {
        if (ConfigHandler.EUSupport) {
            if (storage.getMaxEnergyStored() != storage.getEnergyStored() && storageEU > 0) {
                storage.modifyEnergyStored(MathUtil.truncateDoubleToInt(
                        Math.min(storage.getMaxEnergyStored() - storage.getEnergyStored(),
                                storageEU * ConfigHandler.EUtoRFRatio)));
                storageEU -= Math.min(
                        (storage.getMaxEnergyStored() - storage.getEnergyStored()) / ConfigHandler.EUtoRFRatio,
                        storageEU * ConfigHandler.EUtoRFRatio);
            }
        }
        return Math.max(4000D - storageEU, 0.0D);
    }

    @Optional.Method(modid = "IC2")
    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter tileEntity, EnumFacing facing) {
        return true;
    }

    @Optional.Method(modid = "IC2")
    protected void addToIc2EnergyNetwork() {
        if (!worldObj.isRemote) {
            EnergyTileLoadEvent event = new EnergyTileLoadEvent(this);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Optional.Method(modid = "IC2")
    protected void removeFromIc2EnergyNetwork() {
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

    @Optional.Method(modid = "tesla")
    private BaseOMTeslaContainer getTeslaContainer(Object container) {
        if (container instanceof BaseOMTeslaContainer) {
            return (BaseOMTeslaContainer) container;
        } else {
            container = new BaseOMTeslaContainer();
            return (BaseOMTeslaContainer) container;
        }

    }

    @Optional.Method(modid = "tesla")
    private void moveEnergyFromTeslaToRF() {
        if (teslaContainer instanceof BaseOMTeslaContainer) {
            int energyNeeded = storage.getEnergyStored() - storage.getMaxEnergyStored();
            if (energyNeeded > 0) {
                storage.modifyEnergyStored((int) ((BaseOMTeslaContainer) teslaContainer).takePower(energyNeeded, false));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

        // This method is where other things will try to access your TileEntity's Tesla
        // capability. In the case of the analyzer, is a consumer, producer and holder so we
        // can allow requests that are looking for any of those things. This example also does
        // not care about which side is being accessed, however if you wanted to restrict which
        // side can be used, for example only allow power input through the back, that could be
        // done here.
        if (capability == TeslaCapabilities.CAPABILITY_CONSUMER || capability == TeslaCapabilities.CAPABILITY_HOLDER) {
            if (TeslaLoaded) {
                moveEnergyFromTeslaToRF();
                this.markDirty();
                return (T) getTeslaContainer(this.teslaContainer);
            }
        }

        return super.getCapability(capability, facing);
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
