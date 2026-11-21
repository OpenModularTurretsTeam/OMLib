package omtteam.omlib.platform.fabric;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.omlib.platform.Fuel;

public final class FabricFuel implements Fuel {
    @Override
    public int burnTime(Level level, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        // Fabric API's fuel registration feeds into the level's fuel values
        return Math.max(0, level.fuelValues().burnDuration(stack));
    }

    @Override
    public ItemStack remainder(ItemStack stack) {
        // FabricItem#getRecipeRemainder, the stack-aware remainder hook of Fabric API
        return stack.isEmpty() ? ItemStack.EMPTY : stack.getItem().getRecipeRemainder(stack);
    }
}
