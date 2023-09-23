package omtteam.omlib.util;

import net.minecraft.item.ItemStack;

public class ItemUtil {

    public static boolean isItemStackFuel(ItemStack itemStack) {
        return itemStack.getItem().getItemBurnTime(itemStack) > 0;
    }
}
