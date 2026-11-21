package omtteam.omlib.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.OwnableEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
//? if fabric {
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
//?}
//? if forge {
/*import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
*///?}
import net.minecraft.server.players.GameProfileCache;

import omtteam.omlib.compat.nbt.NbtCompat;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Minecraft API calls behind one set of signatures, so that gameplay code is the same on every version branch.
 * Loader-conditional (Stonecutter) code should live here instead of in gameplay code.
 * <p>
 * Methods taking a {@link HolderLookup.Provider} ignore it on this version; it keeps the call sites the same.
 */
public final class VersionCompat {
    private VersionCompat() {
    }


    // ---- Resource locations ----

    public static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    // ---- Registration (the properties need no registry id on this version) ----

    /** Properties of the block that will be registered as {@code id}. */
    public static BlockBehaviour.Properties blockProperties(BlockBehaviour.Properties properties, ResourceLocation id) {
        return properties;
    }

    /** Properties of the item that will be registered as {@code id}. */
    public static Item.Properties itemProperties(Item.Properties properties, ResourceLocation id) {
        return properties;
    }

    /**
     * Properties of the block item that will be registered as {@code id}. It keeps the block's name
     * ({@code block.<mod>.<id>}).
     */
    public static Item.Properties blockItemProperties(Item.Properties properties, ResourceLocation id) {
        return itemProperties(properties, id);
    }

    /**
     * A block entity type for these blocks.
     */
    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory,
                                                                             Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    /** The entity type of a builder, to be registered as {@code id}. */
    public static <T extends Entity> EntityType<T> buildEntityType(EntityType.Builder<T> builder, ResourceLocation id) {
        return builder.build(id.toString());
    }

    // ---- Entities and players ----

    /** A new entity of this type in the level, {@code null} if it cannot be created. */
    public static <T extends Entity> T createEntity(EntityType<T> type, Level level) {
        return type.create(level);
    }

    /** The UUID of the owner of a tamed or owned entity, {@code null} if it has none. */
    public static @Nullable UUID ownerUuid(OwnableEntity entity) {
        return entity.getOwnerUUID();
    }

    /** Whether the level is in daylight. */
    public static boolean isDay(Level level) {
        return level.isDay();
    }

    /** Plays a sound that only this player hears, at the player's position. */
    public static void playNotifySound(ServerPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        player.playNotifySound(sound, source, volume, pitch);
    }

    // ---- Players and profiles ----

    public static String profileName(GameProfile profile) {
        return profile.getName();
    }

    public static UUID profileId(GameProfile profile) {
        return profile.getId();
    }

    /**
     * Looks a name up in the server's profile cache; a name that is not cached is looked up at Mojang, a blocking web
     * request, so call it off the server thread.
     */
    public static Optional<GameProfile> cachedProfile(MinecraftServer server, String name) {
        GameProfileCache cache = server.getProfileCache();
        return cache == null ? Optional.empty() : cache.get(name);
    }

    /** The name the server's profile cache knows for this player, also while the player is offline. */
    public static Optional<String> cachedName(MinecraftServer server, UUID id) {
        GameProfileCache cache = server.getProfileCache();
        return cache == null ? Optional.empty() : cache.get(id).map(GameProfile::getName);
    }

    /** Whether the player has the highest operator level (OMLib {@code isPlayerOP}: {@code canUseCommand(4, "")}). */
    public static boolean isOwnerOperator(Player player) {
        return player.hasPermissions(4);
    }

    /** Damages an entity (server side). */
    public static boolean hurt(Entity entity, DamageSource source, float amount) {
        return entity.hurt(source, amount);
    }

    /** Whether the player hosts the integrated (singleplayer or LAN) server it is on. */
    public static boolean isSingleplayerOwner(ServerPlayer player) {
        return player.server.isSingleplayerOwner(player.getGameProfile());
    }

    /** Sets an entity on fire for the given number of seconds. */
    public static void setOnFire(Entity entity, int seconds) {
        entity.setSecondsOnFire(seconds);
    }

    // ---- Enchantments ----

    /** Maximum level of the Looting enchantment. */
    public static int maxLootingLevel(Level level) {
        return Enchantments.MOB_LOOTING.getMaxLevel();
    }

    /** Adds Looting at the given level to a stack. */
    public static void enchantLooting(ItemStack stack, Level level, int lootingLevel) {
        stack.enchant(Enchantments.MOB_LOOTING, lootingLevel);
    }

    // ---- ItemStack NBT ----

    /**
     * Serializes a stack (empty stacks become an empty tag).
     */
    public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registries) {
        return stack.save(new CompoundTag());
    }

    /**
     * Deserializes a stack written by {@link #saveStack}; returns {@link ItemStack#EMPTY} on invalid data.
     */
    public static ItemStack loadStack(CompoundTag tag, HolderLookup.Provider registries) {
        return ItemStack.of(tag);
    }

    /**
     * Writes a slot list to {@code tag} under {@code "Items"} (vanilla container format). A stack above its max stack
     * size (in a {@link omtteam.omlib.inventory.LargeStackContainer}) is written with count 1, and its entry gets the
     * real count under {@value #LARGE_COUNT_KEY}: vanilla's own count is a byte and stops at 127.
     */
    public static void saveItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        NonNullList<ItemStack> saved = items;
        boolean large = items.stream().anyMatch(VersionCompat::isLargeStack);
        if (large) {
            saved = NonNullList.withSize(items.size(), ItemStack.EMPTY);
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = items.get(i);
                saved.set(i, isLargeStack(stack) ? stack.copyWithCount(1) : stack);
            }
        }
        ContainerHelper.saveAllItems(tag, saved);
        if (large) {
            ListTag list = NbtCompat.getCompoundList(tag, "Items");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = NbtCompat.getCompound(list, i);
                int slot = savedSlot(entry);
                if (slot < items.size() && isLargeStack(items.get(slot))) {
                    entry.putInt(LARGE_COUNT_KEY, items.get(slot).getCount());
                }
            }
        }
    }

    /**
     * Reads a slot list written by {@link #saveItems}. {@code items} must already have the correct size.
     */
    public static void loadItems(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        ContainerHelper.loadAllItems(tag, items);
        ListTag list = NbtCompat.getCompoundList(tag, "Items");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = NbtCompat.getCompound(list, i);
            int slot = savedSlot(entry);
            if (slot < items.size() && NbtCompat.hasNumber(entry, LARGE_COUNT_KEY) && !items.get(slot).isEmpty()) {
                items.get(slot).setCount(Math.max(1, NbtCompat.getInt(entry, LARGE_COUNT_KEY, 1)));
            }
        }
    }

    /** Entry key of the real count of a stack above its max stack size, see {@link #saveItems}. */
    private static final String LARGE_COUNT_KEY = "OMLibCount";

    private static boolean isLargeStack(ItemStack stack) {
        return stack.getCount() > stack.getMaxStackSize();
    }

    /** The slot of an entry of a vanilla slot list (an unsigned byte). */
    private static int savedSlot(CompoundTag entry) {
        return NbtCompat.getInt(entry, "Slot", 0) & 0xFF;
    }

    // ---- Custom item data (the stack's NBT) ----

    public static boolean hasCustomData(ItemStack stack) {
        return stack.hasTag();
    }

    /**
     * Returns a <b>copy</b> of the custom data of a stack (empty tag if none). Use {@link #setCustomData}
     * or {@link #updateCustomData} to change it.
     */
    public static CompoundTag getCustomData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new CompoundTag() : tag.copy();
    }

    /**
     * Replaces the custom data of a stack. An empty tag removes it.
     */
    public static void setCustomData(ItemStack stack, CompoundTag tag) {
        stack.setTag(tag.isEmpty() ? null : tag.copy());
    }

    /**
     * Modifies the custom data of a stack in place.
     */
    public static void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater) {
        CompoundTag tag = getCustomData(stack);
        updater.accept(tag);
        setCustomData(stack, tag);
    }

    // ---- Blocks ----

    /** Whether bonemeal would work on this block (the game also asks for {@code isClient}, passed as false). */
    public static boolean isValidBonemealTarget(BonemealableBlock block, LevelReader level, BlockPos pos, BlockState state) {
        return block.isValidBonemealTarget(level, pos, state, false);
    }

    // ---- Block entity data on block items (the BlockEntityTag) ----

    /**
     * Stores block entity data on a block item; vanilla loads it into the block entity when the block is placed.
     */
    public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, CompoundTag tag) {
        BlockItem.setBlockEntityData(stack, type, tag);
    }

    public static boolean hasBlockEntityData(ItemStack stack) {
        return BlockItem.getBlockEntityData(stack) != null;
    }

    // ---- Loader differences ----

    /**
     * Builder of a mod creative tab. Fabric places mod tabs on its own tab pages ({@code FabricItemGroup}); Forge
     * sorts mod tabs itself.
     */
    public static CreativeModeTab.Builder creativeTabBuilder() {
        //? if fabric {
        return FabricItemGroup.builder();
        //?} else {
        /*return CreativeModeTab.builder();
        *///?}
    }

    /**
     * The loader's fake player of this level for the profile (one per level and profile, created on first use).
     */
    public static ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        //? if fabric {
        return FakePlayer.get(level, profile);
        //?} else {
        /*return FakePlayerFactory.get(level, profile);
        *///?}
    }

    /** Whether the player is one of the loader's fake players (automation machines such as deployers). */
    public static boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }
}
