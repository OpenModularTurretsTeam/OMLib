package omtteam.omlib.init;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import omtteam.omlib.items.ItemDebugTool;

import static omtteam.omlib.util.InitHelper.registerItem;

/**
 * Created by nico on 23/05/17.
 */
public class OMLibItems {
    public static Item debugTool;

    public static void init(IForgeRegistry<Item> registry) {
        debugTool = registerItem(new ItemDebugTool(), registry);
    }
}
