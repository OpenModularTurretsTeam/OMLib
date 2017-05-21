package omtteam.omlib.util;

import java.util.UUID;

import static omtteam.omlib.handler.ConfigHandler.offlineModeSupport;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public class Player extends Object {
    private UUID uuid;
    private String name;

    public Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Player player) {
        return (offlineModeSupport ^ this.uuid == player.getUuid()) && this.name.equals(player.getName());
    }
}
