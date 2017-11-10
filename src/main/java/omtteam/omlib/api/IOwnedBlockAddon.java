package omtteam.omlib.api;

import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 17/05/17.
 * This interface is for anything that should be bound to an owned block.
 */
public interface IOwnedBlockAddon {

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
    default String getOwner() {
        return this.getLinkedBlock().getOwner();
    }

    /**
     * Return the owner name of the block this block is linked to.
     *
     * @return owner
     */
    default String getOwnerName() {
        return this.getLinkedBlock().getOwnerName();
    }
}
