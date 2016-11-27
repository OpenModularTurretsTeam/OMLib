package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.logging.Logger;

import static omtteam.omlib.util.PlayerUtil.*;


/**
 * Created by Keridos on 24/11/16.
 * This Class
 */
public class TileEntityOwnedBlock extends TileEntity {

    protected String owner = "";
    protected String ownerName = "";


    public TileEntityOwnedBlock() {
        super();
    }


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

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        if (getPlayerUIDUnstable(nbtTagCompound.getString("owner")) != null) {
            this.owner = getPlayerUIDUnstable(nbtTagCompound.getString("owner")).toString();
        } else if (getPlayerUUID(nbtTagCompound.getString("owner")) != null) {
            this.owner = getPlayerUUID(nbtTagCompound.getString("owner")).toString();
        } else {
            Logger.getGlobal().info("Found non existent owner: " + nbtTagCompound.getString(
                    "owner") + "at coordinates: " + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ() + ". Dropping Turretbase");
            worldObj.destroyBlock(this.pos, false);
            return;
        }
        if (nbtTagCompound.hasKey("ownerName")) {
            this.ownerName = nbtTagCompound.getString("ownerName");
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
