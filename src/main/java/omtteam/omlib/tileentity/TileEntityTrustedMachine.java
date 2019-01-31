package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.api.permission.IHasTrustManager;
import omtteam.omlib.api.permission.TrustedPlayersManagerTile;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
public abstract class TileEntityTrustedMachine extends TileEntityMachine implements IHasTrustManager {
    protected TrustedPlayersManagerTile trustManager;

    public TileEntityTrustedMachine() {
        super();
        this.trustManager = new TrustedPlayersManagerTile(this);
    }

    @Override
    public TrustedPlayersManagerTile getTrustManager() {
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
}
