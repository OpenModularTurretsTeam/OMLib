package omtteam.omlib.handler;

import net.minecraft.command.ICommandSender;
import omtteam.omlib.util.CommandTextComponent;
import omtteam.omlib.util.Player;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    void setOwnerShareMap(@Nullable HashMap<Player, ArrayList<Player>> ownerShareMap) {
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
                sender.sendMessage(new CommandTextComponent("Added player " + sharePlayer.getName() + " to your Share List!"));
            }
            ownerShareMap.put(owner, list);
        } else {
            if (!entryFound.getValue().contains(sharePlayer)) {
                entryFound.getValue().add(sharePlayer);
                if (sender != null) {
                    sender.sendMessage(new CommandTextComponent("Added player " + sharePlayer.getName() + " to your Share List!"));
                }
            }
        }
    }

    public void removeSharePlayer(Player owner, Player sharePlayer, @Nullable ICommandSender sender) {
        Map.Entry<Player, ArrayList<Player>> entryFound = null;
        if (owner.equals(sharePlayer)) {
            if (sender != null) {
                sender.sendMessage(new CommandTextComponent("You cannot add/remove yourself to/from your Share List!"));
            }
            return;
        }
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            if (entry.getKey().equals(owner)) {
                entryFound = entry;
            }
        }
        if (entryFound != null) {
            if (entryFound.getValue().contains(sharePlayer)) {
                entryFound.getValue().remove(sharePlayer);
                if (sender != null) {
                    sender.sendMessage(new CommandTextComponent("Removed player " + sharePlayer.getName() + " from your Share List!"));
                }
            }
        } else {
            if (sender != null) {
                sender.sendMessage(new CommandTextComponent("Could not remove player " + sharePlayer.getName() + " from your Share List!"));
            }
        }
    }

    public void printSharePlayers(Player owner, ICommandSender sender) {
        StringBuilder playerList =  new StringBuilder();
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
                if (i <= list.size() - 1) {
                    playerList.append(player.getName());
                    playerList.append(", ");
                } else {
                    playerList.append(player.getName());
                }
            }
        }
        sender.sendMessage(new CommandTextComponent("Players on your share list: " + playerList.toString()));
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
}
