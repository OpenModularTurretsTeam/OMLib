package omtteam.omlib.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Common ({@code c:}) item tags replacing the 1.12.2 ore dictionary entries, as plain tag keys (the same ids on Fabric
 * and NeoForge, e.g. {@code c:ingots/iron}).
 */
public final class CommonTags {
    /** Wrenches of other mods ({@code c:tools/wrench}). */
    public static final TagKey<Item> WRENCHES = tag("tools/wrench");
    /** Ore dictionary {@code ingotIron}. */
    public static final TagKey<Item> IRON_INGOTS = tag("ingots/iron");
    /** Ore dictionary {@code dustRedstone}. */
    public static final TagKey<Item> REDSTONE_DUSTS = tag("dusts/redstone");
    /** Ore dictionary {@code ingotGold}. */
    public static final TagKey<Item> GOLD_INGOTS = tag("ingots/gold");
    /** Ore dictionary {@code gemQuartz}. */
    public static final TagKey<Item> QUARTZ_GEMS = tag("gems/quartz");
    /** Ore dictionary {@code gemDiamond}. */
    public static final TagKey<Item> DIAMOND_GEMS = tag("gems/diamond");
    /** Ore dictionary {@code paneGlass}. */
    public static final TagKey<Item> GLASS_PANES = tag("glass_panes");

    private CommonTags() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, VersionCompat.id("c", path));
    }
}
