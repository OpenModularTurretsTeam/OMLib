package omtteam.omlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Iterator;

/**
 * Created by Keridos on 10/11/17.
 * This Class
 */
public class EntityUtil {
    public static Class<? extends Entity> findClassById(String id) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        return entry == null ? null : entry.getEntityClass();
    }

    public static int getEntityArmor(Entity entity) {
        int armor = 0;
        Iterator<ItemStack> iter = entity.getArmorInventoryList().iterator();
        while (iter.hasNext()) {
            ItemStack itemStack = iter.next();
            armor += itemStack.getItem() instanceof ItemArmor ? ((ItemArmor) itemStack.getItem()).damageReduceAmount : 0;
        }
        return armor;
    }
}
