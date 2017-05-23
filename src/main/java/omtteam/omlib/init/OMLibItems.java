package omtteam.omlib.init;

import net.minecraft.item.Item;
import omtteam.omlib.items.ItemDebugTool;

import static omtteam.omlib.util.InitHelper.registerItem;

/**
 * Created by nico on 23/05/17.
 */
public class OMLibItems {
    public static Item debugTool;

    public static void init() {
        debugTool = registerItem(new ItemDebugTool());
    }

}
