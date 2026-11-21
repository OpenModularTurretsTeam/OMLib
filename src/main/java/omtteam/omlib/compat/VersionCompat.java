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
//? if neoforge {
/*import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
*///?}
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.players.GameProfileCache;

import omtteam.omlib.compat.nbt.NbtCompat;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Hides differences between the supported Minecraft versions.
 * Version-conditional (Stonecutter) code should live here instead of in gameplay code.
 * <p>
 * Methods taking a {@link HolderLookup.Provider} need it on 1.21+; on 1.20.1 it is ignored.
 */
public final class VersionCompat {
    private VersionCompat() {
    }


    // ---- Resource locations ----

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    // ---- Registration (1.21.11 needs the registry id in the properties before a block or item is created) ----

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
     * ({@code block.<mod>.<id>}); 1.21.11 would otherwise name it like an item.
     */
    public static Item.Properties blockItemProperties(Item.Properties properties, ResourceLocation id) {
        return itemProperties(properties, id);
    }

    /**
     * A block entity type for these blocks (1.21.11 removed {@code BlockEntityType.Builder}; vanilla's factory interface
     * is only accessible on NeoForge there, Fabric has its own builder).
     */
    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory,
                                                                             Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    /** The entity type of a builder, to be registered as {@code id} (1.21.11 builds it with the registry key). */
    public static <T extends Entity> EntityType<T> buildEntityType(EntityType.Builder<T> builder, ResourceLocation id) {
        return builder.build(id.toString());
    }

    // ---- Entities and players ----

    /** A new entity of this type in the level (1.21.11 asks for the reason), {@code null} if it cannot be created. */
    public static <T extends Entity> T createEntity(EntityType<T> type, Level level) {
        return type.create(level);
    }

    /** The UUID of the owner of a tamed or owned entity, {@code null} if it has none. */
    public static @Nullable UUID ownerUuid(OwnableEntity entity) {
        return entity.getOwnerUUID();
    }

    /** Whether the level is in daylight (1.21.11 asks for {@code isBrightOutside}). */
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

    /** Damages an entity (server side; 1.21.11 replaced {@code Entity#hurt} with {@code hurtServer}). */
    public static boolean hurt(Entity entity, DamageSource source, float amount) {
        return entity.hurt(source, amount);
    }

    /** Whether the player hosts the integrated (singleplayer or LAN) server it is on. */
    public static boolean isSingleplayerOwner(ServerPlayer player) {
        return player.server.isSingleplayerOwner(player.getGameProfile());
    }

    /** Sets an entity on fire for the given number of seconds. */
    public static void setOnFire(Entity entity, int seconds) {
        entity.igniteForSeconds(seconds);
    }

    // ---- Enchantments ----

    /** Maximum level of the Looting enchantment (data-driven on 1.21, so a datapack may change it). */
    public static int maxLootingLevel(Level level) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING)
                .value().getMaxLevel();
    }

    /** Adds Looting at the given level to a stack. */
    public static void enchantLooting(ItemStack stack, Level level, int lootingLevel) {
        stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
                lootingLevel);
    }

    // ---- ItemStack NBT ----

    /**
     * Serializes a stack (empty stacks become an empty tag).
     */
    public static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider registries) {
        Tag tag = stack.saveOptional(registries);
        return tag instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    /**
     * Deserializes a stack written by {@link #saveStack}; returns {@link ItemStack#EMPTY} on invalid data.
     */
    public static ItemStack loadStack(CompoundTag tag, HolderLookup.Provider registries) {
        return ItemStack.parseOptional(registries, tag);
    }

    /**
     * Writes a slot list to {@code tag} under {@code "Items"} (vanilla container format). A stack above its max stack
     * size (in a {@link omtteam.omlib.inventory.LargeStackContainer}) is written with count 1, and its entry gets the
     * real count under {@value #LARGE_COUNT_KEY}: vanilla's own count stops at 99 (127 on 1.20.1).
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
        ContainerHelper.saveAllItems(tag, saved, registries);
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
        ContainerHelper.loadAllItems(tag, items, registries);
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

    // ---- Custom item data (NBT on 1.20.1, CUSTOM_DATA component on 1.21+) ----

    public static boolean hasCustomData(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /**
     * Returns a <b>copy</b> of the custom data of a stack (empty tag if none). Use {@link #setCustomData}
     * or {@link #updateCustomData} to change it.
     */
    public static CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /**
     * Replaces the custom data of a stack. An empty tag removes it.
     */
    public static void setCustomData(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
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

    /** Whether bonemeal would work on this block (1.20.1 had an extra {@code isClient} parameter). */
    public static boolean isValidBonemealTarget(BonemealableBlock block, LevelReader level, BlockPos pos, BlockState state) {
        return block.isValidBonemealTarget(level, pos, state);
    }

    // ---- Block entity data on block items (BlockEntityTag on 1.20.1, BLOCK_ENTITY_DATA component on 1.21+) ----

    /**
     * Stores block entity data on a block item; vanilla loads it into the block entity when the block is placed.
     */
    public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, CompoundTag tag) {
        BlockItem.setBlockEntityData(stack, type, tag);
    }

    public static boolean hasBlockEntityData(ItemStack stack) {
        return stack.has(DataComponents.BLOCK_ENTITY_DATA);
    }

    // ---- Loader differences ----

    /**
     * Builder of a mod creative tab. Fabric places mod tabs on its own tab pages ({@code FabricItemGroup}); NeoForge
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

    /** Whether the player is one of the loader's fake players (machines such as Create's deployer). */
    public static boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }
}
