package omtteam.omlib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Right-click handling through the loader's block-use event (Fabric {@code UseBlockCallback}, NeoForge and Forge
 * {@code PlayerInteractEvent.RightClickBlock}), which is the same on 1.20.1 and 1.21 (vanilla's {@code use} /
 * {@code useWithoutItem} / {@code useItemOn} differ). Runs before vanilla, also while sneaking; returning
 * {@link InteractionResult#PASS} lets vanilla continue (e.g. placing a held block).
 * <p>
 * The library's loader entrypoints register that event once for all mods. A click goes to the {@link Interceptor}s first
 * (the library has its own for the ownership wand and for claiming; mods register theirs at start-up, for things that act on any block, such as a settings card), then to the
 * block's own {@link #onUse}.
 */
public interface InteractiveBlock {
    InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit);

    /** Handles right-clicks on any block before the block itself does. */
    @FunctionalInterface
    interface Interceptor {
        /** @return {@link InteractionResult#PASS} to let the next interceptor and the block handle the click */
        InteractionResult use(Player player, Level level, InteractionHand hand, BlockHitResult hit);
    }

    /**
     * Adds an interceptor. They run in the order they were added (so in the order the mods start), the first result other
     * than {@link InteractionResult#PASS} ends the click. Call it while the mod starts up.
     */
    static void addInterceptor(Interceptor interceptor) {
        Interceptors.LIST.add(interceptor);
    }

    /**
     * Handles a right-click on any block: the interceptors first, then {@link #onUse} if the block is an
     * {@code InteractiveBlock}. Called by the loader's block-use event on both sides; {@link InteractionResult#PASS} if
     * not handled. Spectators do not interact.
     */
    static InteractionResult handleUse(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) return InteractionResult.PASS;
        for (Interceptor interceptor : Interceptors.LIST) {
            InteractionResult result = interceptor.use(player, level, hand, hit);
            if (result != InteractionResult.PASS) return result;
        }
        BlockState state = level.getBlockState(hit.getBlockPos());
        if (state.getBlock() instanceof InteractiveBlock block) {
            return block.onUse(state, level, hit.getBlockPos(), player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    /**
     * The interceptors (an interface cannot hold private state). The library's own come first: the ownership wand,
     * then claiming an unowned block.
     */
    final class Interceptors {
        private static final List<Interceptor> LIST = new CopyOnWriteArrayList<>(List.of(
                (player, level, hand, hit) -> WandInteraction.use(player, level, hand, hit.getBlockPos()),
                ClaimInteraction::use));

        private Interceptors() {
        }
    }
}
