package omtteam.omlib.platform.fabric;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import omtteam.omlib.platform.Fuel;
import net.fabricmc.fabric.api.registry.FuelRegistry;

public final class FabricFuel implements Fuel {
    @Override
    public int burnTime(Level level, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Integer ticks = FuelRegistry.INSTANCE.get(stack.getItem());
        return ticks == null ? 0 : Math.max(0, ticks);
    }

    @Override
    public ItemStack remainder(ItemStack stack) {
        // FabricItem#getRecipeRemainder, the stack-aware remainder hook of Fabric API
        return stack.isEmpty() ? ItemStack.EMPTY : stack.getItem().getRecipeRemainder(stack);
    }
}
