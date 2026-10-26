package omtteam.omlib.security;

import net.minecraft.core.BlockPos;
import omtteam.omlib.core.security.PlayerIdentity;
import org.jetbrains.annotations.Nullable;

/**
 * A block entity with an owner: blocks with a trust list (through {@link TrustHost}) and blocks with an owner only
 * ({@code OwnedBlockEntity}). Claiming, disowning and the map maker's wand only talk to this interface.
 */
public interface OwnedHost {
    BlockPos getBlockPos();

    /** The rules of the mod this block belongs to. */
    OwnershipSettings ownershipSettings();

    /** The owner, or {@code null} if the block has none (placed without a player, or disowned). */
    @Nullable
    PlayerIdentity getOwner();

    /**
     * Sets the owner only if there is none (claiming by right-click).
     *
     * @return true if the player is now the owner
     */
    boolean claim(PlayerIdentity player);

    /** Sets the owner, replacing any other (placing, the map maker's wand). */
    void setOwner(PlayerIdentity owner);

    /** Removes the owner; blocks with a trust list also clear it. */
    void clearOwner();
}
