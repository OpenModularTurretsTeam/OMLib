package omtteam.omlib.compatibility.minecraft;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public abstract class CompatCreativeTabs extends CreativeTabs {

    public CompatCreativeTabs(String label) {
        super(label);
    }

    protected abstract Item getItem();

    @Override
    public Item getTabIconItem() {
        return getItem();
    }
}