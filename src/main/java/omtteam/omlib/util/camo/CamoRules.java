package omtteam.omlib.util.camo;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;

import javax.annotation.Nullable;

/**
 * Which blocks may be used as a disguise, and the item form of one (1.20.1+ {@code CamoRules}). The rules are those of
 * 3.x {@code handleCamoActivation}: a full block that is drawn from a model and has no tile entity.
 */
public final class CamoRules {
    private CamoRules() {
    }

    @SuppressWarnings("deprecation")
    public static boolean isValidDisguise(IBlockState state) {
        Block block = state.getBlock();
        // camouflaged blocks have a tile entity, so they are excluded too
        return block.isFullBlock(state) && state.getMaterial() != Material.AIR && !block.hasTileEntity(state)
                && state.getRenderType() == EnumBlockRenderType.MODEL;
    }

    /** The disguise a held stack stands for (the item's block with the item's metadata), if it is a valid one. */
    @Nullable
    @SuppressWarnings("deprecation")
    public static IBlockState disguiseFor(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return null;
        }
        ItemBlock item = (ItemBlock) stack.getItem();
        IBlockState state;
        try {
            state = item.getBlock().getStateFromMeta(item.getMetadata(stack.getMetadata()));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
        return isValidDisguise(state) ? state : null;
    }
}
