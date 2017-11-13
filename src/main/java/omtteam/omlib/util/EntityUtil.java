package omtteam.omlib.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Created by Keridos on 10/11/17.
 * This Class
 */
public class EntityUtil {
    public static Class<? extends Entity> findClassById(String id) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        return entry == null ? null : entry.getEntityClass();
    }
}
