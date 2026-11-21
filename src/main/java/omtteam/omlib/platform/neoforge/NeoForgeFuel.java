package omtteam.omlib.platform.neoforge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import omtteam.omlib.platform.Fuel;

public final class NeoForgeFuel implements Fuel {
    @Override
    public int burnTime(Level level, ItemStack stack) {
        // includes NeoForge's furnace_fuels data map and items that override their burn time
        return stack.isEmpty() ? 0 : Math.max(0, stack.getBurnTime(RecipeType.SMELTING));
    }

    @Override
    public ItemStack remainder(ItemStack stack) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.getCraftingRemainingItem();
    }
}
