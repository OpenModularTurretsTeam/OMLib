package omtteam.omlib.api.permission;

import net.minecraft.util.math.BlockPos;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nullable;

/**
 * A tile entity with an owner (1.20.1+ {@code OwnedHost}): OMT's turret bases, OMPD's hardened blocks, walls, fences and
 * camouflage trap, through {@link omtteam.omlib.tileentity.TileEntityOwnedBlock}. Claiming, disowning and the ownership
 * wand only talk to this interface. {@link #getOwner()} keeps its 3.x contract (never null); {@link #getOwnerOrNull()}
 * tells whether there is an owner.
 */
public interface IOwnedHost extends IHasOwner {
    BlockPos getPos();

    /** The owner, or {@code null} if the block has none (placed without a player, or disowned). */
    @Nullable
    Player getOwnerOrNull();

    /**
     * Sets the owner only if there is none (claiming by right-click), and tells the clients.
     *
     * @return true if the player is now the owner
     */
    boolean claim(Player player);

    /** Sets the owner, replacing any other (placing, loading). */
    void setOwner(Player owner);

    /** Removes the owner and tells the clients; blocks with a trust list also clear it. */
    void clearOwner();
}
