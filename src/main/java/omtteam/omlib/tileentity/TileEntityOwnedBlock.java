package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.util.player.Player;

import java.util.UUID;

import static omtteam.omlib.util.player.PlayerUtil.*;

/**
 * Created by Keridos on 24/11/16.
 * This Class
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@MethodsReturnNonnullByDefault
public abstract class TileEntityOwnedBlock extends TileEntityBase {

    protected String owner = "";
    protected String ownerName = "";
    protected String ownerTeamName = "";
    protected boolean dropBlock = false;


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setString("owner", owner);
        if (ownerName.isEmpty() && getPlayerNameFromUUID(owner) != null) {
            ownerName = getPlayerNameFromUUID(owner);
        }
        nbtTagCompound.setString("ownerName", ownerName);
        nbtTagCompound.setString("ownerTeamName", ownerTeamName);
        return nbtTagCompound;
    }

    @SuppressWarnings("ConstantConditions")
    @Override

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
        if (((owner == null || owner.isEmpty()) && !ConfigHandler.offlineModeSupport) || (ConfigHandler.offlineModeSupport && ownerName == null)) {
            dropBlock = true;
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String name) {
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
