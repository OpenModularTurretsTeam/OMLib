package omtteam.omlib.registry;

import net.minecraft.world.inventory.MenuType;
import omtteam.omlib.OMLib;
import omtteam.omlib.compat.MenuCompat;
import omtteam.omlib.menu.CamoMenu;

/**
 * The menu types of the library.
 */
public final class ModMenus {
    /** The camouflage menu of every {@code CamoHost}, whichever mod it belongs to. */
    public static final MenuType<CamoMenu> CAMO = MenuCompat.register(OMLib.id("camo"), CamoMenu::client);

    private ModMenus() {
    }

    /** Loads the class, registering all menu types. */
    public static void init() {
    }
}
