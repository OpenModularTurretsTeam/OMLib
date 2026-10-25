package omtteam.omlib.api.permission;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.AccessProfile;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.SecurityEnvironment;
import omtteam.omlib.core.security.TrustManager;
import omtteam.omlib.util.player.OMLibSecurityEnvironment;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static omtteam.omlib.util.player.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

/**
 * Created by Keridos on 09/02/17.
 * The 3.x trust list of an owned block, now a facade over the core {@link TrustManager}: it converts between the
 * 3.x types ({@link Player}, {@link TrustedPlayer}, {@link EnumAccessLevel}) and the core ones, reads and writes the
 * 3.x NBT and packet formats, and changes the list by copying it and {@link TrustManager#load loading} the copy.
 */
public class TrustedPlayersManager implements ITrustedPlayersManager {
    private final TrustManager core;
    private final SecurityEnvironment environment;
    private final TileEntity tile;
    private Player owner;

    public TrustedPlayersManager(Player owner) {
        this(owner, null);
    }

    public TrustedPlayersManager(Player owner, TileEntity tile) {
        this(owner, tile, OMLibSecurityEnvironment.INSTANCE);
    }

    /** @param environment where the core looks up teams, config and shared owners */
    public TrustedPlayersManager(Player owner, TileEntity tile, SecurityEnvironment environment) {
        this(owner, tile, environment, AccessProfile.ALL);
    }

    /**
     * @param environment where the core looks up teams, config and shared owners
     * @param profile     the access levels the block uses (the camouflage trap:
     *                    {@link AccessProfile#TRUSTED_AND_ADMIN})
     */
    public TrustedPlayersManager(Player owner, TileEntity tile, SecurityEnvironment environment,
                                 AccessProfile profile) {
        // callers mark their tile entity dirty after changes, as in 3.x
        this.core = new TrustManager(environment, () -> {
        }, profile);
        this.environment = environment;
        this.tile = tile;
        setOwner(owner);
    }

    @Override
    public TrustManager core() {
        return core;
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    /**
     * An owner without a UUID (legacy saves) gets the offline UUID of its name in the core; {@code null} (a block
     * without owner) removes the owner and keeps the list.
     */
    public void setOwner(@Nullable Player owner) {
        this.owner = owner;
        PlayerIdentity identity = owner == null ? null : OMLibSecurityEnvironment.toIdentity(owner);
        if (identity != null) {
            core.setOwner(identity);
        } else if (owner == null && core.getOwner() != null) {
            core.load(null, copyEntries());
        }
    }

    /** Removes the owner and clears the trust list (disowning). */
    public void clearOwner() {
        this.owner = null;
        core.clearOwner();
    }

    // ---- reading ----

    @Override
    public TrustedPlayer getTrustedPlayer(String name) {
        for (omtteam.omlib.core.security.TrustedPlayer entry : core.getTrustedPlayers()) {
            if (entry.getName().equalsIgnoreCase(name)) {
                return toApi(entry);
            }
        }
        return null;
    }

    @Override
    public TrustedPlayer getTrustedPlayer(UUID uuid) {
        return core.getTrustedPlayer(uuid).map(TrustedPlayersManager::toApi).orElse(null);
    }

    @Override
    public TrustedPlayer getTrustedPlayer(Player player) {
        PlayerIdentity identity = OMLibSecurityEnvironment.toIdentity(player);
        if (identity == null) {
            return getTrustedPlayer(player.getName());
        }
        return core.getTrustedPlayer(identity).map(TrustedPlayersManager::toApi).orElse(null);
    }

    @Override
    public List<TrustedPlayer> getTrustedPlayers() {
        List<TrustedPlayer> list = new ArrayList<>();
        for (omtteam.omlib.core.security.TrustedPlayer entry : core.getTrustedPlayers()) {
            list.add(toApi(entry));
        }
        return list;
    }

    // ---- changes (no permission checks, see ITrustedPlayersManager) ----

    @Override
    public boolean addTrustedPlayer(String name) {
        Player player = PlayerUtil.getPlayerFromUsernameCache(name);
        return player != null && addTrustedPlayer(new TrustedPlayer(player));
    }

    @Override
    public boolean addTrustedPlayer(TrustedPlayer player) {
        omtteam.omlib.core.security.TrustedPlayer entry = toCore(player);
        if (entry == null || getTrustedPlayer(player.getName()) != null
                || core.getTrustedPlayer(entry.getIdentity()).isPresent() || isOwner(entry.getIdentity())) {
            return false;
        }
        List<omtteam.omlib.core.security.TrustedPlayer> entries = copyEntries();
        entries.add(entry);
        core.load(core.getOwner(), entries);
        return true;
    }

    @Override
    public boolean removeTrustedPlayer(String name) {
        List<omtteam.omlib.core.security.TrustedPlayer> entries = copyEntries();
        if (!entries.removeIf(entry -> entry.getName().equalsIgnoreCase(name))) {
            return false;
        }
        core.load(core.getOwner(), entries);
        return true;
    }

    @Override
    public void setTrustedPlayers(List<TrustedPlayer> trustedPlayers) {
        List<omtteam.omlib.core.security.TrustedPlayer> entries = new ArrayList<>();
        for (TrustedPlayer player : trustedPlayers) {
            omtteam.omlib.core.security.TrustedPlayer entry = toCore(player);
            if (entry != null) {
                entries.add(entry);
            }
        }
        core.load(core.getOwner(), entries);
    }

    @Override
    public boolean changePermission(String player, EnumAccessLevel level) {
        return changeLevel(player, level.toCore());
    }

    @Override
    public boolean changePermission(String player, int change) {
        TrustedPlayer entry = getTrustedPlayer(player);
        if (entry == null) {
            return false;
        }
        AccessLevel newLevel = core.getProfile().step(entry.getAccessLevel().toCore(), change);
        return newLevel != null && changeLevel(player, newLevel);
    }

    private boolean changeLevel(String name, AccessLevel level) {
        List<omtteam.omlib.core.security.TrustedPlayer> entries = copyEntries();
        boolean found = false;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getName().equalsIgnoreCase(name)) {
                entries.set(i, new omtteam.omlib.core.security.TrustedPlayer(entries.get(i).getIdentity(), level));
                found = true;
            }
        }
        if (found) {
            core.load(core.getOwner(), entries);
        }
        return found;
    }

    private boolean isOwner(PlayerIdentity player) {
        PlayerIdentity coreOwner = core.getOwner();
        return coreOwner != null && (coreOwner.is(player.uuid())
                || (environment.offlineModeSupport() && coreOwner.name().equalsIgnoreCase(player.name())));
    }

    private List<omtteam.omlib.core.security.TrustedPlayer> copyEntries() {
        List<omtteam.omlib.core.security.TrustedPlayer> entries = new ArrayList<>();
        for (omtteam.omlib.core.security.TrustedPlayer entry : core.getTrustedPlayers()) {
            entries.add(entry.copy());
        }
        return entries;
    }

    // ---- conversion ----

    private static TrustedPlayer toApi(omtteam.omlib.core.security.TrustedPlayer entry) {
        TrustedPlayer player = new TrustedPlayer(entry.getName());
        player.setUuid(entry.getUuid());
        player.setAccessLevel(EnumAccessLevel.fromCore(entry.getAccessLevel()));
        return player;
    }

    /**
     * The core entry. A missing UUID is looked up by name, and in offline mode it is the name's offline UUID;
     * {@code null} if there is none.
     */
    private omtteam.omlib.core.security.TrustedPlayer toCore(TrustedPlayer player) {
        UUID uuid = player.getUuid() != null ? player.getUuid() : getPlayerUUID(player.getName());
        if (uuid == null && environment.offlineModeSupport()) {
            uuid = OMLibSecurityEnvironment.offlineUuid(player.getName());
        }
        if (uuid == null) {
            return null;
        }
        return new omtteam.omlib.core.security.TrustedPlayer(new PlayerIdentity(uuid, player.getName()),
                                                             player.getAccessLevel().toCore());
    }

    // ---- NBT and packets (3.x formats) ----

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList nbtList = new NBTTagList();
        for (omtteam.omlib.core.security.TrustedPlayer entry : core.getTrustedPlayers()) {
            NBTTagCompound nbtPlayer = new NBTTagCompound();
            nbtPlayer.setString("name", entry.getName());
            nbtPlayer.setInteger("accessLevel", entry.getAccessLevel().ordinal());
            nbtPlayer.setString("UUID", entry.getUuid().toString());
            nbtList.appendTag(nbtPlayer);
        }
        nbt.setTag("trustedPlayers", nbtList);
        return nbt;
    }

    /**
     * Reads every format 3.x wrote: entries with {@code accessLevel} (ordinal), older ones with the flags
     * {@code admin} / {@code canChangeTargeting} / {@code canOpenGUI}, and the oldest plain list of names. Entries
     * whose UUID cannot be found are dropped, as in 3.x. {@code useGlobal} (global trust lists, never reachable) is
     * ignored.
     */
    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        List<TrustedPlayer> players = new ArrayList<>();
        NBTTagList list = tagCompound.getTagList("trustedPlayers", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbtPlayer = list.getCompoundTagAt(i);
            if (nbtPlayer.getString("name").isEmpty()) {
                continue;
            }
            TrustedPlayer trustedPlayer = new TrustedPlayer(nbtPlayer.getString("name"));
            trustedPlayer.setAccessLevel(readLevel(nbtPlayer));
            trustedPlayer.setUuid(nbtPlayer.hasKey("UUID") ? getPlayerUIDUnstable(nbtPlayer.getString("UUID"))
                                          : getPlayerUUID(trustedPlayer.getName()));
            players.add(trustedPlayer);
        }
        NBTTagList names = tagCompound.getTagList("trustedPlayers", Constants.NBT.TAG_STRING);
        for (int i = 0; i < names.tagCount(); i++) {
            players.add(new TrustedPlayer(names.getStringTagAt(i)));
        }
        setTrustedPlayers(players);
    }

    private static EnumAccessLevel readLevel(NBTTagCompound nbtPlayer) {
        if (nbtPlayer.hasKey("accessLevel")) {
            int ordinal = nbtPlayer.getInteger("accessLevel");
            return ordinal >= 0 && ordinal < EnumAccessLevel.values().length ? EnumAccessLevel.values()[ordinal]
                    : EnumAccessLevel.NONE;
        } else if (nbtPlayer.getBoolean("admin")) {
            return EnumAccessLevel.ADMIN;
        } else if (nbtPlayer.getBoolean("canChangeTargeting")) {
            return EnumAccessLevel.CHANGE_SETTINGS;
        } else if (nbtPlayer.getBoolean("canOpenGUI")) {
            return EnumAccessLevel.OPEN_GUI;
        }
        return EnumAccessLevel.NONE;
    }

    @Override
    public void readFromByteBuf(ByteBuf buf) {
        List<TrustedPlayer> players = new ArrayList<>();
        int lengthOfPlayerList = buf.readInt();
        for (int j = 0; j < lengthOfPlayerList; j++) {
            TrustedPlayer player = new TrustedPlayer(Player.readFromByteBuf(buf));
            player.setAccessLevel(EnumAccessLevel.values()[buf.readInt()]);
            players.add(player);
        }
        setTrustedPlayers(players);
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        buf.writeInt(core.getTrustedPlayers().size());
        for (omtteam.omlib.core.security.TrustedPlayer entry : core.getTrustedPlayers()) {
            Player.writeToByteBuf(new Player(entry.getUuid(), entry.getName(), ""), buf);
            buf.writeInt(entry.getAccessLevel().ordinal());
        }
    }

    @Override
    public boolean hasTile() {
        return tile != null;
    }

    @Override
    public TileEntity getTile() {
        return tile;
    }
}
