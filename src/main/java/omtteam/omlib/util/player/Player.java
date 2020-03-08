package omtteam.omlib.util.player;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import omtteam.omlib.handler.OMConfig;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Created by Keridos on 17/05/17.
 * This Class is a wrapper for a player in SMP.
 */
public class Player {
    private final UUID uuid;
    private final String name;
    private String teamName = "";

    public Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.teamName = getTeamNameFromServer();
    }

    public Player(UUID uuid, String name, String teamName) {
        this.uuid = uuid;
        this.name = name;
        this.teamName = teamName;
    }

    public Player(EntityPlayer player) {
        this.uuid = player.getUniqueID();
        this.name = player.getName();
        if (player.getTeam() != null) {
            this.teamName = player.getTeam().getName();
        }
    }

    public static void writeToByteBuf(Player player, ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, player.getName());
        ByteBufUtils.writeUTF8String(buf, player.getUuid().toString());
        ByteBufUtils.writeUTF8String(buf, player.getTeamName());
    }

    public static Player readFromByteBuf(ByteBuf buf) {
        String name = ByteBufUtils.readUTF8String(buf);
        UUID uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        String teamUUID = ByteBufUtils.readUTF8String(buf);
        return new Player(uuid, name, teamUUID);
    }

    public static Player readFromNBT(NBTTagCompound tag) {
        return new Player(tag.getUniqueId("uuid"),
                          tag.getString("name"),
                          tag.getString("team_name"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setString("name", this.name);
        tag.setUniqueId("uuid", this.uuid);
        if (!this.teamName.equalsIgnoreCase("")) {
            tag.setString("team_name", this.teamName);
        }
        return tag;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return (!OMConfig.GENERAL.offlineModeSupport ? getUuid().equals(player.getUuid())
                : getName().toLowerCase().equals(player.getName().toLowerCase()));
    }

    @Override
    public int hashCode() {
        int result = getUuid() != null ? getUuid().hashCode() : 0;
        result = 31 * result + getName().toLowerCase().hashCode();
        return result;
    }

    @Nullable
    public EntityPlayer getEntityPlayer() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return server.getPlayerList().getPlayerByUUID(this.uuid);
    }

    private String getTeamNameFromServer() {
        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null && server.getPlayerList().getPlayerByUUID(this.uuid) != null) {
                Team team = server.getPlayerList().getPlayerByUUID(this.uuid).getTeam();
                if (team != null) {
                    return team.getName();
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return "";
    }
}
