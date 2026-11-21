package omtteam.omlib.compat;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.level.redstone.Orientation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.LevelReader;

/**
 * Base class for all mod blocks with a block entity, the same on every version branch (the counterpart of
 * {@link CompatBlockEntity}).
 * <p>
 * It gives blocks one set of hooks:
 * <ul>
 *     <li>the block codec (the game registers the block type through it): create one with {@link #codecOf} in a
 *     static field and pass it to the constructor</li>
 *     <li>pick block ({@code getCloneItemStack} takes a {@code LevelReader} and a flag): override {@link #pickItem}
 *     instead</li>
 *     <li>neighbour updates ({@code neighborChanged} gets an {@code Orientation} instead of the neighbour's position):
 *     override {@link #onNeighborChanged}</li>
 *     <li>light absorption ({@code getLightBlock} and {@code propagatesSkylightDown} take no level and position):
 *     override {@link #lightAbsorbed} / {@link #skylightPropagates}</li>
 * </ul>
 */
public abstract class CompatEntityBlock extends BaseEntityBlock {
    private final BlockCodec<?> blockCodec;

    protected CompatEntityBlock(BlockCodec<?> blockCodec, Properties properties) {
        super(properties);
        this.blockCodec = blockCodec;
    }

    /**
     * The codec of a block that is created from its properties only.
     *
     * @param factory the block's constructor
     */
    protected static <T extends BaseEntityBlock> BlockCodec<T> codecOf(Function<Properties, T> factory) {
        return new BlockCodec<>(simpleCodec(factory));
    }

    /**
     * The codec of a block that is created from one value and its properties (a tier, a turret type).
     *
     * @param name    the key of the value in the codec
     * @param field   how the value is stored
     * @param getter  reads the value from the block
     * @param factory the block's constructor
     */
    protected static <T extends BaseEntityBlock, A> BlockCodec<T> codecOf(String name, Codec<A> field, Function<T, A> getter,
                                                                          BiFunction<A, Properties, T> factory) {
        return new BlockCodec<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                field.fieldOf(name).forGetter(getter),
                propertiesCodec()
        ).apply(instance, factory)));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return blockCodec.codec();
    }

    /**
     * What picking the block gives (middle click), e.g. a camouflage instead of the block itself.
     *
     * @param picked the item vanilla would give
     * @return the block's own item by default
     */
    protected ItemStack pickItem(BlockGetter level, BlockPos pos, ItemStack picked) {
        return picked;
    }

    /**
     * A neighbouring block changed (or the block was told to check).
     *
     * @param neighbourPos always {@code null}: the game passes an {@code Orientation} instead of the neighbour's
     *                     position
     */
    protected void onNeighborChanged(BlockState state, Level level, BlockPos pos, @Nullable BlockPos neighbourPos) {
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbourBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbourBlock, orientation, movedByPiston);
        onNeighborChanged(state, level, pos, null);
    }

    /** How much light the block absorbs (0 to 15); {@code vanilla} is what the block would absorb by default. */
    protected int lightAbsorbed(BlockState state, int vanilla) {
        return vanilla;
    }

    /** Whether sky light passes through the block; {@code vanilla} is the default. */
    protected boolean skylightPropagates(BlockState state, boolean vanilla) {
        return vanilla;
    }

    @Override
    protected int getLightBlock(BlockState state) {
        return lightAbsorbed(state, super.getLightBlock(state));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return skylightPropagates(state, super.propagatesSkylightDown(state));
    }

    @Override
    protected @NotNull ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return pickItem(level, pos, super.getCloneItemStack(level, pos, state, includeData));
    }
}
