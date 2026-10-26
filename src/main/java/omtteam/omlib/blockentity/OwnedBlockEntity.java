package omtteam.omlib.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import omtteam.omlib.compat.CompatBlockEntity;
import omtteam.omlib.compat.nbt.NbtCompat;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.security.OwnedHost;
import omtteam.omlib.security.OwnershipSettings;
import omtteam.omlib.security.PlayerAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * A block with an owner and nothing else (no trust list), e.g. hardened walls: only the owner counts. A mod registers a
 * block entity type for it (through a small subclass that passes the type and its settings) and shows ownership like on
 * any {@link OwnedHost}.
 */
public class OwnedBlockEntity extends CompatBlockEntity implements OwnedHost {
    @Nullable
    private PlayerIdentity owner;

    private final OwnershipSettings settings;

    public OwnedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, OwnershipSettings settings) {
        super(type, pos, state);
        this.settings = settings;
    }

    @Override
    public OwnershipSettings ownershipSettings() {
        return settings;
    }

    @Nullable
    @Override
    public PlayerIdentity getOwner() {
        return owner;
    }

    @Override
    public void setOwner(PlayerIdentity owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
        syncToClient();
    }

    @Override
    public boolean claim(PlayerIdentity player) {
        Objects.requireNonNull(player, "player");
        if (owner != null) return false;
        setOwner(player);
        return true;
    }

    @Override
    public void clearOwner() {
        owner = null;
        syncToClient();
    }

    /** Whether the player owns the block. Blocks without owner (placed by machines, or disowned) belong to nobody. */
    public boolean isOwner(Player player) {
        return owner != null && owner.is(player.getUUID());
    }

    /** Makes the placing player the owner (call from the block's {@code setPlacedBy}). */
    public static void setOwnerOnPlace(@Nullable OwnedBlockEntity blockEntity, @Nullable LivingEntity placer) {
        if (blockEntity != null && placer instanceof Player player) {
            blockEntity.setOwner(PlayerAdapter.identity(player));
        }
    }

    @Override
    protected void readData(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag ownerTag = NbtCompat.getCompound(tag, "Owner");
        UUID uuid = NbtCompat.getUuid(ownerTag, "UUID");
        owner = uuid != null ? new PlayerIdentity(uuid, NbtCompat.getString(ownerTag, "Name", "")) : null;
    }

    @Override
    protected void writeData(CompoundTag tag, HolderLookup.Provider registries) {
        if (owner != null) {
            CompoundTag ownerTag = new CompoundTag();
            NbtCompat.putUuid(ownerTag, "UUID", owner.uuid());
            ownerTag.putString("Name", owner.name());
            tag.put("Owner", ownerTag);
        }
    }
}
