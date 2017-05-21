package omtteam.omlib.handler;

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

    public void addSharePlayer(Player owner, Player sharePlayer) {
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
            ownerShareMap.put(owner, list);
        } else {
            if (!entryFound.getValue().contains(sharePlayer)) {
                entryFound.getValue().add(sharePlayer);
            }
        }
    }

    public void removeSharePlayer(Player owner, Player sharePlayer) {
        Map.Entry<Player, ArrayList<Player>> entryFound = null;
        if (owner.equals(sharePlayer)) {
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
            }
        }
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
