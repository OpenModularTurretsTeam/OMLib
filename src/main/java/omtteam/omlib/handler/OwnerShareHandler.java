package omtteam.omlib.handler;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;
import omtteam.omlib.network.messages.MessageSetSharePlayerList;
import omtteam.omlib.util.Player;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public class OwnerShareHandler implements Serializable {
    private static OwnerShareHandler instance;
    private HashMap<Player, ArrayList<Player>> ownerShareMap;

    private OwnerShareHandler() {
        ownerShareMap = new HashMap<>();
    }

    public static OwnerShareHandler getInstance() {
        if (instance == null) {
            instance = new OwnerShareHandler();
        }
        return instance;
    }

    public HashMap<Player, ArrayList<Player>> getOwnerShareMap() {
        return ownerShareMap;
    }

    public void setOwnerShareMap(@Nullable HashMap<Player, ArrayList<Player>> ownerShareMap) {
        if (ownerShareMap != null) {
            this.ownerShareMap = ownerShareMap;
        }
    }

    @Nullable
    public Map.Entry<Player, ArrayList<Player>> getEntryFromName(String name) {
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            if (entry.getKey().getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private Map.Entry<Player, ArrayList<Player>> getEntry(Player player) {
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            if (entry.getKey().equals(player)) {
                return entry;
            }
        }
        return null;
    }

    public void addSharePlayer(Player owner, Player sharePlayer, @Nullable ICommandSender sender) {
        Map.Entry<Player, ArrayList<Player>> entryFound = null;
        if (owner.equals(sharePlayer)) {
            return;
        }
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            if (entry.getKey().equals(owner)) {
                entryFound = entry;
            }
        }
        if (entryFound == null) {
            ArrayList<Player> list = new ArrayList<>();
            list.add(sharePlayer);
            if (sender != null) {
                sender.addChatMessage(new TextComponentString("Added player " + sharePlayer.getName() + " to your Share List!"));
            }
            ownerShareMap.put(owner, list);
        } else {
            if (!entryFound.getValue().contains(sharePlayer)) {
                entryFound.getValue().add(sharePlayer);
                if (sender != null) {
                    sender.addChatMessage(new TextComponentString("Added player " + sharePlayer.getName() + " to your Share List!"));
                }
            }
        }
        OMLibNetworkingHandler.sendMessgeToAllPlayers(new MessageSetSharePlayerList(this));
    }

    public void removeSharePlayer(Player owner, Player sharePlayer, @Nullable ICommandSender sender) {
        Map.Entry<Player, ArrayList<Player>> entryFound = null;
        if (owner.equals(sharePlayer)) {
            if (sender != null) {
                sender.addChatMessage(new TextComponentString("You cannot add/remove yourself to/from your Share List!"));
            }
            return;
        }
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            if (entry.getKey().equals(owner)) {
                entryFound = entry;
            }
        }
        if (entryFound != null) {
            int sizeBefore = entryFound.getValue().size();
            Predicate<Player> predicate = p -> p.equals(sharePlayer);
            entryFound.getValue().removeIf(predicate);
            if (sender != null) {
                if (entryFound.getValue().size() < sizeBefore) {
                    sender.addChatMessage(new TextComponentString("Removed player " + sharePlayer.getName() + " from your Share List!"));
                } else {
                    sender.addChatMessage(new TextComponentString("Could not remove player " + sharePlayer.getName() + " from your Share List!"));
                }
            }
        } else if (sender != null) {
            sender.addChatMessage(new TextComponentString("Could not remove player " + sharePlayer.getName() + " from your Share List!"));
        }
        OMLibNetworkingHandler.sendMessgeToAllPlayers(new MessageSetSharePlayerList(this));
    }

    public void printSharePlayers(Player owner, ICommandSender sender) {
        StringBuilder playerList = new StringBuilder();
        Map.Entry<Player, ArrayList<Player>> entryFound = null;
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            if (entry.getKey().equals(owner)) {
                entryFound = entry;
            }
        }
        if (entryFound != null) {
            ArrayList<Player> list = entryFound.getValue();
            for (int i = 0; i < list.size(); i++) {
                Player player = list.get(i);
                if (i < list.size() - 1) {
                    playerList.append(player.getName());
                    playerList.append(", ");
                } else {
                    playerList.append(player.getName());
                }
            }
        }
        sender.addChatMessage(new TextComponentString("Players on your share list: " + playerList.toString()));
    }

    public boolean isPlayerSharedOwner(Player owner, Player shareCheck) {
        Map.Entry<Player, ArrayList<Player>> entry = getEntry(owner);
        if (entry != null) {
            for (Player player : entry.getValue()) {
                if (player.equals(shareCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    static void saveToDisk() {
        File saveRoot = DimensionManager.getCurrentSaveRootDirectory();
        if (saveRoot != null) {
            Path path = Paths.get(saveRoot.toString() + "/omlib/");
            Path fullpath = Paths.get(saveRoot.toString() + "/omlib/OwnerShare.sav");
            try {
                if (Files.notExists(path)) {
                    if (path.toFile().mkdir()) {
                        throw new Exception("Failed to create dir");
                    }
                }
                if (getInstance() != null && getInstance().getOwnerShareMap() != null) {
                    FileOutputStream saveFile = new FileOutputStream(fullpath.toFile());
                    ObjectOutputStream save = new ObjectOutputStream(saveFile);
                    save.writeObject(getInstance().getOwnerShareMap());
                    save.close();
                    saveFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.deleteIfExists(fullpath);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    static void loadFromDisk() {
        HashMap<Player, ArrayList<Player>> input = new HashMap<>();
        try {
            Path fullpath = Paths.get(DimensionManager.getCurrentSaveRootDirectory().toString() + "/omlib/OwnerShare.sav");
            FileInputStream saveFile = new FileInputStream(fullpath.toFile());
            ObjectInputStream save = new ObjectInputStream(saveFile);
            Object object = save.readObject();
            if (object instanceof HashMap) {
                input = (HashMap<Player, ArrayList<Player>>) object;
            }
            getInstance().setOwnerShareMap(input);
            save.close();
            saveFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
