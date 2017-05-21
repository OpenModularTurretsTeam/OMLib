package omtteam.omlib.util;

import net.minecraftforge.common.DimensionManager;
import omtteam.omlib.handler.OwnerShareHandler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public class OwnerShareDiskIO {
        public static void saveToDisk(OwnerShareHandler input) {
            Path path = Paths.get(DimensionManager.getCurrentSaveRootDirectory().toString() + "/omlib/");
            Path fullpath = Paths.get(DimensionManager.getCurrentSaveRootDirectory().toString() + "/omlib/OwnerShare.sav");
            try {
                if (Files.notExists(path)) {
                    if (path.toFile().mkdir()) {
                        throw new Exception("Failed to create dir");
                    }
                }
                FileOutputStream saveFile = new FileOutputStream(fullpath.toFile());
                ObjectOutputStream save = new ObjectOutputStream(saveFile);
                save.writeObject(input.getOwnerShareMap());
                save.close();
                saveFile.close();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.deleteIfExists(fullpath);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        public static HashMap<Player, ArrayList<Player>>  loadFromDisk() {
            HashMap<Player, ArrayList<Player>>  input = null;
            try {
                Path fullpath = Paths.get(DimensionManager.getCurrentSaveRootDirectory().toString() + "/omlib/OwnerShare.sav");
                FileInputStream saveFile = new FileInputStream(fullpath.toFile());
                ObjectInputStream save = new ObjectInputStream(saveFile);
                input = (HashMap<Player, ArrayList<Player>>) save.readObject();
                save.close();
                saveFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return input;
        }

}
