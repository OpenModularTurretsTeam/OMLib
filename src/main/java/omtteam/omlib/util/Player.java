package omtteam.omlib.util;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
public class Player {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (getUuid() != null ? !getUuid().equals(player.getUuid()) : player.getUuid() != null) return false;
        return getName().toLowerCase().equals(player.getName().toLowerCase());
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + getName().toLowerCase().hashCode();
        return result;
    }

    public static void writeToByteBuf(Player player, ByteBuf buf) {
        buf.writeInt(player.getName().length());
        buf.writeBytes(player.getName().getBytes());
        buf.writeInt(player.getUuid().toString().length());
        buf.writeBytes(player.getUuid().toString().getBytes());
    }

    public static Player readFromByteBuf(ByteBuf buf) {
        int length = buf.readInt();
        String name = new String(buf.readBytes(length).array());

        length = buf.readInt();
        UUID uuid = UUID.fromString(new String(buf.readBytes(length).array()));
        return new Player(uuid, name);
    }
}
