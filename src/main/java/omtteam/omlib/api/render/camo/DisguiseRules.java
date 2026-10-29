package omtteam.omlib.api.render.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import omtteam.omlib.util.camo.CamoRules;

import javax.annotation.Nullable;

/**
 * Which blocks a mod's {@link ICamoSupport} hosts accept as a disguise (1.20.1+ {@code DisguiseRules}). The camouflage
 * menu asks the host for its rules, so the mods decide for themselves (any full block, or only opaque ones, for
 * example).
 */
public interface DisguiseRules {
    /**
     * OMLib's rules: full blocks drawn from a model, without a tile entity unless they are plain-looking blocks, and
     * see-through ones (glass, ice, leaves) too, as on 1.20.1+ ({@link CamoRules}).
     */
    DisguiseRules DEFAULT = state -> CamoRules.isValidDisguise(state, true);

    /** Whether this block state may be used as a disguise. */
    boolean isValidDisguise(IBlockState state);

    /** The disguise a held stack stands for (the item's block with the item's metadata), if it is a valid one. */
    @Nullable
    default IBlockState disguiseFor(ItemStack stack) {
        IBlockState state = CamoRules.stateFor(stack);
        return state != null && isValidDisguise(state) ? state : null;
    }

    /** What the player is told when a block is refused as a disguise; a mod can explain its own rule. */
    default ITextComponent invalidMessage() {
        return new TextComponentTranslation("text.omlib.camo_invalid");
    }

    /** Translation key of the empty disguise slot's tooltip (client); a mod can say which blocks work. */
    default String slotHint() {
        return "tooltip.omlib.camouflage_slot";
    }
}
