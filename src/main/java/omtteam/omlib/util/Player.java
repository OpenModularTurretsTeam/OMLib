package omtteam.omlib.util;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

import static omtteam.omlib.handler.ConfigHandler.offlineModeSupport;

/**
 * Created by Keridos on 17/05/17.
 * This Class is a wrapper for a player in SMP.
 */
public class Player {
    private final UUID uuid;
    private String name;

    public Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public static void writeToByteBuf(Player player, ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, player.name);
        ByteBufUtils.writeUTF8String(buf, player.getUuid().toString());
    }

    public static Player readFromByteBuf(ByteBuf buf) {
        String name = ByteBufUtils.readUTF8String(buf);
        UUID uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        return new Player(uuid, name);
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

        return (!offlineModeSupport ? getUuid().equals(player.getUuid())
                : getName().toLowerCase().equals(player.getName().toLowerCase()));
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + getName().toLowerCase().hashCode();
        return result;
    }
}
