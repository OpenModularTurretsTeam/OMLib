package omtteam.omlib.camo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Which blocks a mod's {@link CamoHost}s accept as a disguise. The camouflage menu asks the host for its rules, so the
 * mods decide for themselves (any full block, or only opaque ones, for example).
 */
public interface DisguiseRules {
    /** Whether this block state may be used as a disguise. */
    boolean isValidDisguise(BlockState state);

    /** The disguise a held stack stands for (the item's block in its default state), if it is a valid one. */
    @Nullable
    default BlockState disguiseFor(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return null;
        BlockState state = item.getBlock().defaultBlockState();
        return isValidDisguise(state) ? state : null;
    }

    /** What the player is told when a block is refused as a disguise; a mod can explain its own rule. */
    default Component invalidMessage() {
        return Component.translatable("message.omlib.camo_invalid");
    }

    /** The tooltip of the empty disguise slot (client); a mod can say which blocks work. */
    default Component slotHint() {
        return Component.translatable("tooltip.omlib.camouflage_slot");
    }

    /** The item shown in the disguise slot of the menu. */
    default ItemStack itemFor(@Nullable BlockState camo) {
        return camo == null ? ItemStack.EMPTY : new ItemStack(camo.getBlock());
    }
}
