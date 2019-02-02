package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

import static omtteam.omlib.util.player.PlayerUtil.*;

/**
 * Created by Keridos on 24/11/16.
 * This Class
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@MethodsReturnNonnullByDefault
public abstract class TileEntityOwnedBlock extends TileEntityBase implements IHasOwner {

    protected String owner = "";
    protected String ownerName = "";
    protected String ownerTeamName = "";
    protected boolean dropBlock = false;

    @Override
    @ParametersAreNonnullByDefault
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setString("owner", getOwner());
        if (this.getOwnerName().isEmpty() && getPlayerNameFromUUID(getOwner()) != null) {
            ownerName = getPlayerNameFromUUID(getOwner()) != null ? getPlayerNameFromUUID(getOwner()) : "";
        }
        nbtTagCompound.setString("ownerName", this.getOwnerName());
        nbtTagCompound.setString("ownerTeamName", ownerTeamName);
        return nbtTagCompound;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @ParametersAreNonnullByDefault
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        if (getPlayerUIDUnstable(nbtTagCompound.getString("owner")) != null) {
            this.owner = getPlayerUIDUnstable(nbtTagCompound.getString("owner")).toString();
        } else if (getPlayerUUID(nbtTagCompound.getString("owner")) != null) {
            this.owner = getPlayerUUID(nbtTagCompound.getString("owner")).toString();
        }
        if (nbtTagCompound.hasKey("ownerName")) {
            this.ownerName = nbtTagCompound.getString("ownerName");
        }
        if (nbtTagCompound.hasKey("ownerTeamName")) {
            this.ownerName = nbtTagCompound.getString("ownerTeamName");
        }
        if (((owner == null || owner.isEmpty()) && !OMConfig.GENERAL.offlineModeSupport) || (OMConfig.GENERAL.offlineModeSupport && ownerName.isEmpty())) {
            dropBlock = true;
        }
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(@Nonnull String name) {
        ownerName = name;
    }

    public String getOwnerTeamName() {
        return ownerTeamName;
    }

    public void setOwnerTeamName(String ownerTeamName) {
        this.ownerTeamName = ownerTeamName;
    }

    public Player getOwnerAsPlayer() {
        if (ownerTeamName.isEmpty()) {
            Player player = new Player(UUID.fromString(owner), ownerName);
            this.ownerTeamName = player.getTeamName();
            return player;
        } else {
            return new Player(UUID.fromString(owner), ownerName, ownerTeamName);
        }
    }
}
