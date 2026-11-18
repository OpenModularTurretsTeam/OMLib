package omtteam.omlib.api.permission;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.AccessProfile;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.PlayerInfo;
import omtteam.omlib.core.security.SecurityEnvironment;
import omtteam.omlib.core.security.TrustManager;
import omtteam.omlib.core.security.TrustResult;
import omtteam.omlib.util.player.OMLibSecurityEnvironment;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import static omtteam.omlib.util.player.PlayerUtil.getPlayerUIDUnstable;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

/**
 * Created by Keridos on 09/02/17.
 * The 3.x trust list of an owned block, now a facade over the core {@link TrustManager}: it converts between the
 * 3.x types ({@link Player}, {@link TrustedPlayer}, {@link EnumAccessLevel}) and the core ones, reads and writes the
 * 3.x NBT and packet formats, and changes the list through the core's change methods (see {@link #change}).
 */
public class TrustedPlayersManager implements ITrustedPlayersManager {
    /**
     * The acting owner of changes to a list without owner: no player has this UUID (it is neither a Mojang nor an
     * offline UUID) or this name.
     */
    private static final PlayerIdentity PLACEHOLDER_OWNER = new PlayerIdentity(new UUID(0L, 0L), "");

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
    // The changes go through the core's change methods, as on 1.20.1/1.21.1/1.21.11: names are resolved to the entries'
    // UUIDs, the owner acts (the callers have checked the player's rights), and a TrustResult other than SUCCESS is
    // the 3.x false.

    @Override
    public boolean addTrustedPlayer(String name) {
        Player player = PlayerUtil.getPlayerFromUsernameCache(name);
        return player != null && addTrustedPlayer(new TrustedPlayer(player));
    }

    /**
     * Adds the player at its level; false if it has no UUID, is the owner, is already on the list (also by name), or
     * its level is not one of the block's {@link AccessProfile} levels.
     */
    @Override
    public boolean addTrustedPlayer(TrustedPlayer player) {
        omtteam.omlib.core.security.TrustedPlayer entry = toCore(player);
        if (entry == null || getTrustedPlayer(player.getName()) != null
                || !core.getProfile().allows(entry.getAccessLevel())) {
            return false;
        }
        return change((trust, actor) -> {
            TrustResult added = trust.addTrustedPlayer(actor, entry.getIdentity());
            if (added != TrustResult.SUCCESS || entry.getAccessLevel() == AccessLevel.NONE) {
                return added;
            }
            return trust.setAccessLevel(actor, entry.getUuid(), entry.getAccessLevel());
        });
    }

    /** Removes every entry with this name (ignoring case). */
    @Override
    public boolean removeTrustedPlayer(String name) {
        return changeEach(name, (trust, actor, uuid) -> trust.removeTrustedPlayer(actor, uuid));
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

    /**
     * Sets the level of every entry with this name (ignoring case); false if there is none or the level is not one of
     * the block's {@link AccessProfile} levels.
     */
    @Override
    public boolean changePermission(String player, EnumAccessLevel level) {
        return changeEach(player, (trust, actor, uuid) -> trust.setAccessLevel(actor, uuid, level.toCore()));
    }

    /** Raises or lowers every entry with this name (ignoring case) by that many levels of the block's profile. */
    @Override
    public boolean changePermission(String player, int change) {
        return changeEach(player, (trust, actor, uuid) -> trust.changeAccessLevel(actor, uuid, change));
    }

    /** A change of one trust-list entry through the core. */
    private interface EntryChange {
        TrustResult apply(TrustManager trust, PlayerInfo actor, UUID player);
    }

    /** Applies the change to every entry with this name; true if it succeeded for at least one. */
    private boolean changeEach(String name, EntryChange change) {
        List<UUID> matches = new ArrayList<>();
        for (omtteam.omlib.core.security.TrustedPlayer entry : core.getTrustedPlayers()) {
            if (entry.getName().equalsIgnoreCase(name)) {
                matches.add(entry.getUuid());
            }
        }
        boolean changed = false;
        for (UUID uuid : matches) {
            changed |= change((trust, actor) -> change.apply(trust, actor, uuid));
        }
        return changed;
    }

    /**
     * Runs a change of the core's change methods with the owner as the acting player. A list without owner (an
     * unowned block) has no admin in the core, so the change runs on a copy managed by a placeholder owner and is
     * loaded back if it succeeded.
     *
     * @return whether the change succeeded
     */
    private boolean change(BiFunction<TrustManager, PlayerInfo, TrustResult> change) {
        PlayerIdentity coreOwner = core.getOwner();
        if (coreOwner != null) {
            return change.apply(core, new PlayerInfo(coreOwner.uuid(), coreOwner.name(), false)) == TrustResult.SUCCESS;
        }
        TrustManager copy = new TrustManager(environment, () -> {
        }, core.getProfile());
        copy.load(PLACEHOLDER_OWNER, copyEntries(core));
        if (change.apply(copy, new PlayerInfo(PLACEHOLDER_OWNER.uuid(), PLACEHOLDER_OWNER.name(), false))
                != TrustResult.SUCCESS) {
            return false;
        }
        core.load(null, copyEntries(copy));
        return true;
    }

    private List<omtteam.omlib.core.security.TrustedPlayer> copyEntries() {
        return copyEntries(core);
    }

    private static List<omtteam.omlib.core.security.TrustedPlayer> copyEntries(TrustManager trust) {
        List<omtteam.omlib.core.security.TrustedPlayer> entries = new ArrayList<>();
        for (omtteam.omlib.core.security.TrustedPlayer entry : trust.getTrustedPlayers()) {
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
