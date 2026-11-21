package omtteam.omlib.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//? if neoforge {
/*import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
*///?}

/**
 * Version-independent base class for all mod block entities.
 * <p>
 * Subclasses override {@link #readData} / {@link #writeData} instead of the vanilla NBT methods, whose
 * signatures differ between 1.20.1 and 1.21. Everything written by {@link #writeData} is also sent to
 * clients (initial chunk data and {@link #syncToClient()}); override {@link #writeClientData} to send less.
 */
public abstract class CompatBlockEntity extends BlockEntity {
    //? if neoforge {
    /*/^* The {@link #renderData()} in the block entity's model data on NeoForge and Forge. ^/
    public static final ModelProperty<Object> RENDER_DATA = new ModelProperty<>();
    *///?}

    protected CompatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Loads state from NBT. Also called on the client for synced data.
     */
    protected abstract void readData(CompoundTag tag, HolderLookup.Provider registries);

    /**
     * Saves state to NBT.
     */
    protected abstract void writeData(CompoundTag tag, HolderLookup.Provider registries);

    /**
     * Data sent to clients. Defaults to the full saved data.
     */
    protected void writeClientData(CompoundTag tag, HolderLookup.Provider registries) {
        writeData(tag, registries);
    }

    /**
     * Marks the block entity dirty and sends its client data to all tracking players.
     */
    public void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * What the block's model reads while the chunk is meshed (the camouflage), {@code null} for nothing. Fabric reads
     * it as block entity render data, NeoForge and Forge as {@link #RENDER_DATA} in the model data. Call {@link #remesh} on the
     * client when it changed.
     */
    protected @Nullable Object renderData() {
        return null;
    }

    //? if fabric {
    @Override
    public @Nullable Object getRenderData() {
        return renderData();
    }
    //?} else {
    /*@Override
    public ModelData getModelData() {
        Object data = renderData();
        return data == null ? ModelData.EMPTY : ModelData.builder().with(RENDER_DATA, data).build();
    }
    *///?}

    /** Makes the client redraw the block, e.g. after its {@link #renderData()} changed (no effect on the server). */
    public void remesh() {
        if (level != null && level.isClientSide()) {
            //? if neoforge {
            /*requestModelDataUpdate();
            *///?}
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_IMMEDIATE);
        }
    }

    /**
     * The block was broken or replaced by another block (not called when only its state changes), just before the
     * block entity is removed. Server only. Vanilla calls it on 1.21.11 ({@code preRemoveSideEffects}); on older
     * versions the block's {@code onRemove} has to call {@link #handleRemove}.
     */
    protected void onBlockRemoved(Level level) {
    }

    /**
     * Forwards a block's {@code onRemove} to {@link #onBlockRemoved} with the same conditions as vanilla 1.21.11.
     * Call it first thing in {@code onRemove}, before {@code super.onRemove} removes the block entity.
     */
    public static void handleRemove(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof CompatBlockEntity blockEntity) {
            blockEntity.onBlockRemoved(level);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readData(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeData(tag, registries);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        writeClientData(tag, registries);
        return tag;
    }
}
