package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.handler.ConfigHandler;

import static omtteam.omlib.util.PlayerUtil.*;


/**
 * Created by Keridos on 24/11/16.
 * This Class
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@MethodsReturnNonnullByDefault
public abstract class TileEntityOwnedBlock extends TileEntityBase {

    protected String owner = "";
    protected String ownerName = "";
    protected boolean dropBlock = false;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setString("owner", owner);
        if (ownerName.isEmpty() && getPlayerNameFromUUID(owner) != null) {
            ownerName = getPlayerNameFromUUID(owner);
        }
        nbtTagCompound.setString("ownerName", ownerName);
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
        if (((owner == null || owner.isEmpty()) && !ConfigHandler.offlineModeSupport) || (ConfigHandler.offlineModeSupport && ownerName == null)){
            dropBlock =true;
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
}
