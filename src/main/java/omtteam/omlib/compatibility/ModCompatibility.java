package omtteam.omlib.compatability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import omtteam.omlib.OMLib;
import omtteam.omlib.reference.Reference;

/**
 * Created by Keridos on 23/01/2015. This Class
 */
@SuppressWarnings("WeakerAccess")
public class ModCompatibility {
    public static boolean IC2Loaded = false;
    public static boolean TeslaLoaded = false;
    public static boolean CoFHApiLoaded = false;
    public static boolean OpenComputersLoaded = false;
    public static boolean ComputerCraftLoaded = false;
    public static boolean TOPLoaded = false;

    public static final String IC2ModId = "ic2";
    public static final String TeslaModId = "tesla";
    public static final String CoFHApiModId = "CoFHAPI";
    public static final String OCModID = "OpenComputers";
    public static final String CCModID = "ComputerCraft";
    public static final String MekModID = "Mekanism";
    public static final String TEModID = "ThermalExpansion";
    public static final String EIOModID = "EnderIO";
    public static final String TCModID = "Thaumcraft";
    public static final String TOPModID = "theoneprobe";

    public static void checkForMods() {
        IC2Loaded = Loader.isModLoaded(IC2ModId);
        TeslaLoaded = Loader.isModLoaded(TeslaModId);
        OpenComputersLoaded = Loader.isModLoaded(OCModID);
        ComputerCraftLoaded = Loader.isModLoaded(CCModID);
        TOPLoaded = Loader.isModLoaded(TOPModID);
        fixIC2Loading();

        printDetectedMods();
    }

    private static void printDetectedMods() {
        String foundMods = "Found the following mods: ";
        foundMods += IC2Loaded ? "IC2 " : "";
        foundMods += TeslaLoaded ? "Tesla " : "";
        foundMods += CoFHApiLoaded ? "CoFHApi " : "";
        foundMods += OpenComputersLoaded ? "OpenComputers " : "";
        foundMods += TOPLoaded ? "The One Probe " : "";
        OMLib.getLogger().info(foundMods);
    }

    public static void fixIC2Loading() {
       /* if (IC2Loaded) {
            try {
                Class.forName("ic2.api.energy.tile.IEnergySink", false, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException e) {
                IC2Loaded = false;
                Logger.getLogger("OMlib").severe("IC2 should be present but class not found!");
            }
        }  */
    }

    private static void addVersionCheckerInfo() {
        NBTTagCompound versionchecker = new NBTTagCompound();
        versionchecker.setString("curseProjectName", "omlib");
        versionchecker.setString("curseFilenameParser", "OMLib-1.10.2-[].jar");
        versionchecker.setString("modDisplayName", "OMLib");
        versionchecker.setString("oldVersion", Reference.VERSION);
        FMLInterModComms.sendRuntimeMessage("omlib", "VersionChecker", "addCurseCheck", versionchecker);
    }

    public static void performModCompat() {
        addVersionCheckerInfo();
        if (TOPLoaded) {
            TOPCompatibility.register();
        }
    }
}
