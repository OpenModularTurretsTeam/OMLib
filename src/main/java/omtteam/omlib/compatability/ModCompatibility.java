package omtteam.omlib.compatability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import omtteam.omlib.reference.Reference;

import java.util.logging.Logger;

/**
 * Created by Keridos on 23/01/2015. This Class
 */
public class ModCompatibility {
    public static boolean IC2Loaded = false;
    public static boolean TeslaLoaded = false;
    private static Logger logger;

    public static void checkForMods() {

        IC2Loaded = Loader.isModLoaded("IC2");
        TeslaLoaded = Loader.isModLoaded("tesla");
    }

    private static void addVersionCheckerInfo() {
        NBTTagCompound versionchecker = new NBTTagCompound();
        versionchecker.setString("curseProjectName", "224663-omtteam.openmodularturrets");
        versionchecker.setString("curseFilenameParser", "OMLib-1.10.2-[].jar");
        versionchecker.setString("modDisplayName", "OpenModularTurrets");
        versionchecker.setString("oldVersion", Reference.VERSION);
        FMLInterModComms.sendRuntimeMessage("omtteam/omlib", "VersionChecker", "addCurseCheck", versionchecker);
    }

    public static void performModCompat() {
        addVersionCheckerInfo();
    }

}
