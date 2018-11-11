package omtteam.omlib.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import omtteam.omlib.util.player.TrustedPlayer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "CanBeFinal", "unused"})
public abstract class TileEntityTrustedMachine extends TileEntityMachine implements ITrustedPlayersManager {
    protected List<TrustedPlayer> trustedPlayers;

    public TileEntityTrustedMachine() {
        super();
        this.trustedPlayers = new ArrayList<>();
    }

    @Override
    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void setTrustedPlayers(List<TrustedPlayer> list) {
        this.trustedPlayers = list;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("trustedPlayers", getTrustedPlayersAsNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        buildTrustedPlayersFromNBT(tag.getTagList("trustedPlayers", 10));
    }
}
