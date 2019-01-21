package omtteam.omlib.init;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import omtteam.omlib.items.FakeSword;
import omtteam.omlib.items.ItemDebugTool;

import java.util.ArrayList;
import java.util.List;

import static omtteam.omlib.util.InitHelper.registerItem;

/**
 * Created by nico on 23/05/17.
 */
public class OMLibItems {
    public static Item debugTool;
    public static Item fakeSword;
    public static List<Item> subBlocks = new ArrayList<>();

    public static void init(IForgeRegistry<Item> registry) {
        debugTool = registerItem(new ItemDebugTool(), registry);
        fakeSword = registerItem(new FakeSword(Item.ToolMaterial.DIAMOND), registry);
        for (Item item : subBlocks) {
            registry.register(item);
        }
    }
}
