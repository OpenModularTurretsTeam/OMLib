package omtteam.omlib.tileentity;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import omtteam.omlib.api.permission.IOwnedHost;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nullable;
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
public abstract class TileEntityOwnedBlock extends TileEntityBase implements IOwnedHost {
    @Nullable
    private Player owner;

    @Override
    @ParametersAreNonnullByDefault
    public NBTTagCompound saveToNBT(NBTTagCompound nbtTagCompound) {
        super.saveToNBT(nbtTagCompound);
        NBTTagCompound tag = new NBTTagCompound();
        if (this.owner != null) {
            this.owner.writeToNBT(tag);
            nbtTagCompound.setTag("owner", tag);
        }

        return nbtTagCompound;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadFromNBT(NBTTagCompound nbtTagCompound) {
        super.loadFromNBT(nbtTagCompound);
        // no owner key: the block has no owner (also on clients after a disown)
        this.owner = null;

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
    }

    /** The owner; a placeholder named {@code no_owner} if there is none (3.x contract, see {@link #getOwnerOrNull}). */
    @Override
    public Player getOwner() {
        return owner != null ? owner : new Player(UUID.randomUUID(), "no_owner");
    }

    @Nullable
    @Override
    public Player getOwnerOrNull() {
        return owner;
    }

    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    public boolean claim(Player player) {
        if (owner != null) {
            return false;
        }
        setOwner(player);
        ownerChanged();
        return true;
    }

    /** Subclasses with a trust list clear it too and call this. */
    @Override
    public void clearOwner() {
        this.owner = null;
        ownerChanged();
    }

    /** Saves and tells the clients (the unowned sparkle, menus and tooltips depend on the owner). */
    protected void ownerChanged() {
        markDirty();
        if (world != null && !world.isRemote) {
            syncToClient();
        }
    }
}
