package omtteam.omlib.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Common ({@code c:}) item tags replacing the 1.12.2 ore dictionary entries, as plain tag keys (the same ids on Fabric
 * and Forge, e.g. {@code c:iron_ingots}). Forge has no common tags; the library's Forge jar defines them from the
 * matching {@code forge:} tags ({@code src/main/forge/resources}), except {@link #WRENCHES}.
 * <p>
 * There are no common tags for redstone/lapis blocks and wooden chests; recipes use the vanilla items there
 * ({@code Items.REDSTONE_BLOCK}, {@code Items.LAPIS_BLOCK}, {@code Items.CHEST}).
 */
public final class CommonTags {
    /** Wrenches of other mods ({@code c:wrenches}). */
    public static final TagKey<Item> WRENCHES = tag("wrenches");
    /** Ore dictionary {@code ingotIron}. */
    public static final TagKey<Item> IRON_INGOTS = tag("iron_ingots");
    /** Ore dictionary {@code dustRedstone}. */
    public static final TagKey<Item> REDSTONE_DUSTS = tag("redstone_dusts");
    /** Ore dictionary {@code ingotGold}. */
    public static final TagKey<Item> GOLD_INGOTS = tag("gold_ingots");
    /** Ore dictionary {@code gemQuartz}. */
    public static final TagKey<Item> QUARTZ_GEMS = tag("quartz");
    /** Ore dictionary {@code gemDiamond}. */
    public static final TagKey<Item> DIAMOND_GEMS = tag("diamonds");
    /** Ore dictionary {@code paneGlass}. */
    public static final TagKey<Item> GLASS_PANES = tag("glass_panes");

    private CommonTags() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, VersionCompat.id("c", path));
    }
}
