package omtteam.omlib.api.permission;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import omtteam.omlib.core.security.TrustManager;
import omtteam.omlib.util.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The 3.x trust list API of owned blocks, kept as a facade over the core {@link TrustManager} (see
 * {@link TrustedPlayersManager}). The change methods do not check permissions: callers check them first, as in 3.x.
 * New code should use {@link #core()}.
 */
public interface ITrustedPlayersManager extends IHasOwner {
    /** The core trust manager behind this facade; all permission decisions are made there. */
    TrustManager core();

    /** Adds the player with this name (looked up in the username cache) at {@link EnumAccessLevel#NONE}. */
    boolean addTrustedPlayer(String name);

    /** Adds the player at its access level; false if it has no UUID, is the owner or is already on the list. */
    boolean addTrustedPlayer(TrustedPlayer player);

    /** Removes the player with this name (ignoring case). */
    boolean removeTrustedPlayer(String name);

    /** The entry with this name (ignoring case), or {@code null}. */
    TrustedPlayer getTrustedPlayer(String name);

    /** The entry with this UUID, or {@code null}. */
    TrustedPlayer getTrustedPlayer(UUID uuid);

    /** The entry of this player (by UUID, or by name in offline mode), or {@code null}. */
    TrustedPlayer getTrustedPlayer(Player player);

    /** A copy of the trust list; changing it changes nothing, use the change methods. */
    List<TrustedPlayer> getTrustedPlayers();

    /** Replaces the trust list (entries without UUID, for the owner or duplicates are skipped). */
    void setTrustedPlayers(List<TrustedPlayer> trustedPlayers);

    default List<Map<String, String>> getTrustedPlayersAsListMap() {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (TrustedPlayer tp : getTrustedPlayers()) {
            list.add(tp.asMap());
        }
        return list;
    }

    NBTTagCompound writeToNBT(NBTTagCompound nbt);

    /** Reads the trust list in every format 3.x wrote. */
    void readFromNBT(NBTTagCompound tagCompound);

    void readFromByteBuf(ByteBuf buf);

    void writeToByteBuf(ByteBuf buf);

    /** Sets the level of the player with this name (ignoring case). */
    boolean changePermission(String player, EnumAccessLevel level);

    /** Raises ({@code +1}) or lowers ({@code -1}) the level of the player with this name (ignoring case). */
    boolean changePermission(String player, int change);

    boolean hasTile();

    TileEntity getTile();
}
