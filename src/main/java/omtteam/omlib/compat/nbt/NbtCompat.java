package omtteam.omlib.compat.nbt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/**
 * Reads NBT the same way on every version branch: the tag's own getters return {@code Optional}s, these methods give
 * the fallback (or empty) for a missing key or a wrong type. Read NBT only through these methods.
 * <p>
 * Depends on Minecraft only, so any package may use it (it is outside the package cycle around {@code compat}).
 */
public final class NbtCompat {
    private NbtCompat() {
    }

    public static int getInt(CompoundTag tag, String key, int fallback) {
        return tag.getIntOr(key, fallback);
    }

    public static long getLong(CompoundTag tag, String key, long fallback) {
        return tag.getLongOr(key, fallback);
    }

    public static float getFloat(CompoundTag tag, String key, float fallback) {
        return tag.getFloatOr(key, fallback);
    }

    public static double getDouble(CompoundTag tag, String key, double fallback) {
        return tag.getDoubleOr(key, fallback);
    }

    public static boolean getBoolean(CompoundTag tag, String key, boolean fallback) {
        return tag.getBooleanOr(key, fallback);
    }

    public static String getString(CompoundTag tag, String key, String fallback) {
        return tag.getStringOr(key, fallback);
    }

    /** The int array under {@code key}, empty if there is none. */
    public static int[] getIntArray(CompoundTag tag, String key) {
        return tag.getIntArray(key).orElseGet(() -> new int[0]);
    }

    /** The long array under {@code key}, empty if there is none. */
    public static long[] getLongArray(CompoundTag tag, String key) {
        return tag.getLongArray(key).orElseGet(() -> new long[0]);
    }

    /** The compound under {@code key}, empty if there is none. */
    public static CompoundTag getCompound(CompoundTag tag, String key) {
        return tag.getCompoundOrEmpty(key);
    }

    /** The list of compounds under {@code key}, empty if there is none. */
    public static ListTag getCompoundList(CompoundTag tag, String key) {
        return tag.getListOrEmpty(key);
    }

    /** The compound at {@code index} of a list of compounds, empty if there is none. */
    public static CompoundTag getCompound(ListTag list, int index) {
        return list.getCompoundOrEmpty(index);
    }

    /** Whether there is a compound under {@code key}. */
    public static boolean hasCompound(CompoundTag tag, String key) {
        return tag.getCompound(key).isPresent();
    }

    /** Whether there is a list under {@code key}. */
    public static boolean hasList(CompoundTag tag, String key) {
        return tag.getList(key).isPresent();
    }

    /** Whether there is a string under {@code key}. */
    public static boolean hasString(CompoundTag tag, String key) {
        return tag.getString(key).isPresent();
    }

    /** Whether there is a number under {@code key}. */
    public static boolean hasNumber(CompoundTag tag, String key) {
        return tag.get(key) instanceof net.minecraft.nbt.NumericTag;
    }

    /** The UUID stored by {@link #putUuid}, {@code null} if there is none (or it is invalid). */
    public static @Nullable UUID getUuid(CompoundTag tag, String key) {
        return tag.read(key, UUIDUtil.CODEC).orElse(null);
    }

    public static void putUuid(CompoundTag tag, String key, UUID uuid) {
        tag.store(key, UUIDUtil.CODEC, uuid);
    }

    /** A block state written by {@link NbtUtils#writeBlockState}; air if it is invalid. */
    public static BlockState readBlockState(CompoundTag tag) {
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag);
    }
}
