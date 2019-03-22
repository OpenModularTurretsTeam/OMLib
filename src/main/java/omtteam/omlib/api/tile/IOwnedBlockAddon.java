package omtteam.omlib.api.tile;

import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 17/05/17.
 * This interface is for anything that should be bound to an owned block.
 */
public interface IOwnedBlockAddon extends IHasOwner {

    /**
     * Return the owned block this block is linked to.
     *
     * @return TileEntityOwnedBlock
     */
    @Nonnull
    TileEntityOwnedBlock getLinkedBlock();

    /**
     * Return the owner of the block this block is linked to.
     *
     * @return owner
     */
    @Override
    default Player getOwner() {
        return this.getLinkedBlock().getOwner();
    }
}
