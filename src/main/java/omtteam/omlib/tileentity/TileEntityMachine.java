package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.common.Optional;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.power.OMEnergyStorage;
import omtteam.omlib.util.TrustedPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static omtteam.omlib.util.PlayerUtil.getPlayerUUID;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
@Optional.InterfaceList({
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")})

public abstract class TileEntityMachine extends TileEntityContainerElectric implements ITrustedPlayersManager {
    private boolean active;
    protected boolean inverted;
    private boolean redstone;
    protected List<TrustedPlayer> trustedPlayers;

    public TileEntityMachine() {
        super();
        this.trustedPlayers = new ArrayList<>();
        this.storage = new OMEnergyStorage(10, 10);
        this.inverted = true;
        this.active = true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addTrustedPlayer(String name) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(name);
        trustedPlayer.uuid = getPlayerUUID(name);

        if (!this.getWorld().isRemote) {
            boolean foundPlayer = false;
            for (Map.Entry<UUID, String> serverName : UsernameCache.getMap().entrySet()) {
                if (name.equals(serverName.getValue())) {
                    foundPlayer = true;
                    break;
                }
            }

            if (!foundPlayer) {
                return false;
            }
        }

        if (ConfigHandler.offlineModeSupport) {
            if (trustedPlayer.getName().equals(getOwner())) {
                return false;
            }

        } else {
            if (trustedPlayer.uuid == null || trustedPlayer.uuid.toString().equals(getOwner())) {
                return false;
            }
        }

        if (trustedPlayer.uuid != null || ConfigHandler.offlineModeSupport) {
            for (TrustedPlayer player : trustedPlayers) {
                if (ConfigHandler.offlineModeSupport) {
                    if (player.getName().toLowerCase().equals(name.toLowerCase()) || player.getName().equals(getOwner())) {
                        return false;
                    }
                } else {
                    if (player.getName().toLowerCase().equals(name.toLowerCase()) || trustedPlayer.uuid.toString().equals(
                            owner)) {
                        return false;
                    }
                }
            }
            trustedPlayers.add(trustedPlayer);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTrustedPlayer(String name) {
        for (TrustedPlayer player : trustedPlayers) {
            if (player.getName().equals(name)) {
                trustedPlayers.remove(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void setTrustedPlayers(List<TrustedPlayer> list) {
        this.trustedPlayers = list;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setBoolean("active", active);
        nbtTagCompound.setBoolean("inverted", inverted);
        nbtTagCompound.setBoolean("redstone", redstone);
        nbtTagCompound.setTag("trustedPlayers", getTrustedPlayersAsNBT());
        return nbtTagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.active = !nbtTagCompound.hasKey("active") || nbtTagCompound.getBoolean("active");
        this.inverted = !nbtTagCompound.hasKey("inverted") || nbtTagCompound.getBoolean("inverted");
        this.redstone = nbtTagCompound.getBoolean("redstone");
        buildTrustedPlayersFromNBT(nbtTagCompound.getTagList("trustedPlayers", 10));
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
}
