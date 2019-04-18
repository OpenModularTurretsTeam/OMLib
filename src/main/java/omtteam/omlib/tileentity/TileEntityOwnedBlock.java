package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.util.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

import static omtteam.omlib.util.player.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

/**
 * Created by Keridos on 24/11/16.
 * This Class
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@MethodsReturnNonnullByDefault
public abstract class TileEntityOwnedBlock extends TileEntityBase implements IHasOwner {
    private Player owner;
    protected boolean dropBlock = false;

    @Override
    @ParametersAreNonnullByDefault
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        NBTTagCompound tag = new NBTTagCompound();
        if (this.getOwner() != null) {
            this.owner.writeToNBT(tag);
            nbtTagCompound.setTag("owner", tag);
        }

        return nbtTagCompound;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);

        if (nbtTagCompound.hasKey("ownerName")) { // TODO: Remove in 1.13
            UUID uuid = null;
            if (getPlayerUIDUnstable(nbtTagCompound.getString("owner")) != null) {
                uuid = getPlayerUIDUnstable(nbtTagCompound.getString("owner"));
            } else if (getPlayerUUID(nbtTagCompound.getString("owner")) != null) {
                uuid = getPlayerUUID(nbtTagCompound.getString("owner"));
            }
            String ownerName = nbtTagCompound.getString("ownerName");
            Player owner = new Player(uuid, nbtTagCompound.getString("ownerName"));
            if (nbtTagCompound.hasKey("ownerTeamName")) {
                owner.setTeamName(nbtTagCompound.getString("ownerTeamName"));
            }
            this.owner = owner;
        }
        if (nbtTagCompound.hasKey("owner", Constants.NBT.TAG_COMPOUND)) {
            this.owner = Player.readFromNBT((NBTTagCompound) nbtTagCompound.getTag("owner"));
        }

        if (owner == null) {
            dropBlock = true;
        }
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
