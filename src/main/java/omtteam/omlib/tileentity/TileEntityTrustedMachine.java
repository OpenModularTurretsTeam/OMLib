package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.api.network.ISyncableTE;
import omtteam.omlib.api.permission.TrustedPlayersManager;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.util.player.Player;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
public abstract class TileEntityTrustedMachine extends TileEntityMachine implements IHasTrustManager, ISyncableTE {
    protected TrustedPlayersManager trustManager;

    public TileEntityTrustedMachine() {
        super();
        this.trustManager = new TrustedPlayersManager(this.getOwner(), this);
    }

    @Override
    public TrustedPlayersManager getTrustManager() {
        return trustManager;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        trustManager.writeToNBT(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        trustManager.readFromNBT(tag);
    }

    @Override
    public void setOwner(Player owner) {
        this.trustManager.setOwner(owner);
        super.setOwner(owner);
    }
}
