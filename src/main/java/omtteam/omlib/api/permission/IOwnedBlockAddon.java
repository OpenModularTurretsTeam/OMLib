package omtteam.omlib.api.permission;

import omtteam.omlib.tileentity.TileEntityOwnedBlock;

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
    default String getOwner() {
        return this.getLinkedBlock().getOwner();
    }

    /**
     * Return the owner name of the block this block is linked to.
     *
     * @return ownerName
     */
    @Override
    default String getOwnerName() {
        return this.getLinkedBlock().getOwnerName();
    }

    /**
     * Return the owners team of the block this block is linked to.
     *
     * @return owners team
     */
    @Override
    default String getOwnerTeamName() {
        return this.getLinkedBlock().getOwnerTeamName();
    }
}
