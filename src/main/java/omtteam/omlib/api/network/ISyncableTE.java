package omtteam.omlib.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.ListIterator;

public interface ISyncableTE extends ISyncable {
    List<EntityPlayerMP> getSyncPlayerList();

    TileEntity getTE();

    void informUpdate();

    default void scrubSyncPlayerList() {
        ListIterator<EntityPlayerMP> iter = getSyncPlayerList().listIterator();
        while (iter.hasNext()) {
            EntityPlayerMP player = iter.next();
            TileEntity te = getTE();
            if (player.isDead || player.world.provider.getDimension() != te.getWorld().provider.getDimension()
                    || player.getDistance(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()) > 25) {
                iter.remove();
            }
        }
    }
}
