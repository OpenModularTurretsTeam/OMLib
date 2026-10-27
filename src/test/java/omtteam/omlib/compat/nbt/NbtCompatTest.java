package omtteam.omlib.compat.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs on every node, so it checks that each version's branch reads NBT the same way. */
class NbtCompatTest {
    private static CompoundTag tag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("int", 42);
        tag.putByte("byte", (byte) 7);
        tag.putLong("long", 1L << 40);
        tag.putFloat("float", 1.5f);
        tag.putDouble("double", 2.25);
        tag.putBoolean("boolean", true);
        tag.putString("string", "text");
        tag.putIntArray("ints", new int[]{1, 2, 3});
        tag.putLongArray("longs", new long[]{4L, 5L});
        CompoundTag child = new CompoundTag();
        child.putInt("value", 9);
        tag.put("compound", child);
        ListTag list = new ListTag();
        list.add(child.copy());
        tag.put("list", list);
        return tag;
    }

    @Test
    void storedValuesAreRead() {
        CompoundTag tag = tag();
        assertEquals(42, NbtCompat.getInt(tag, "int", -1));
        assertEquals(1L << 40, NbtCompat.getLong(tag, "long", -1));
        assertEquals(1.5f, NbtCompat.getFloat(tag, "float", -1));
        assertEquals(2.25, NbtCompat.getDouble(tag, "double", -1));
        assertTrue(NbtCompat.getBoolean(tag, "boolean", false));
        assertEquals("text", NbtCompat.getString(tag, "string", ""));
        assertArrayEquals(new int[]{1, 2, 3}, NbtCompat.getIntArray(tag, "ints"));
        assertArrayEquals(new long[]{4L, 5L}, NbtCompat.getLongArray(tag, "longs"));
        assertEquals(9, NbtCompat.getInt(NbtCompat.getCompound(tag, "compound"), "value", -1));
        assertEquals(9, NbtCompat.getInt(NbtCompat.getCompound(NbtCompat.getCompoundList(tag, "list"), 0), "value", -1));
    }

    @Test
    void numbersOfAnotherNumericTypeAreConverted() {
        CompoundTag tag = tag();
        assertEquals(7, NbtCompat.getInt(tag, "byte", -1));
        assertEquals(42L, NbtCompat.getLong(tag, "int", -1));
        assertEquals(42.0, NbtCompat.getDouble(tag, "int", -1));
        assertTrue(NbtCompat.getBoolean(tag, "byte", false));
    }

    @Test
    void missingKeysReadAsTheFallbackOrEmpty() {
        CompoundTag tag = new CompoundTag();
        assertEquals(-1, NbtCompat.getInt(tag, "missing", -1));
        assertEquals(-1L, NbtCompat.getLong(tag, "missing", -1));
        assertEquals(-1f, NbtCompat.getFloat(tag, "missing", -1));
        assertEquals(-1.0, NbtCompat.getDouble(tag, "missing", -1));
        assertTrue(NbtCompat.getBoolean(tag, "missing", true));
        assertEquals("fallback", NbtCompat.getString(tag, "missing", "fallback"));
        assertEquals(0, NbtCompat.getIntArray(tag, "missing").length);
        assertEquals(0, NbtCompat.getLongArray(tag, "missing").length);
        assertTrue(NbtCompat.getCompound(tag, "missing").isEmpty());
        assertTrue(NbtCompat.getCompoundList(tag, "missing").isEmpty());
        assertNull(NbtCompat.getUuid(tag, "missing"));
    }

    @Test
    void valuesOfTheWrongTypeReadAsTheFallback() {
        CompoundTag tag = tag();
        assertEquals(-1, NbtCompat.getInt(tag, "string", -1));
        assertEquals(-1L, NbtCompat.getLong(tag, "compound", -1));
        assertEquals("fallback", NbtCompat.getString(tag, "int", "fallback"));
        assertTrue(NbtCompat.getCompound(tag, "int").isEmpty());
        assertTrue(NbtCompat.getCompoundList(tag, "int").isEmpty());
    }

    @Test
    void aListIndexOutOfRangeReadsAsAnEmptyCompound() {
        ListTag list = NbtCompat.getCompoundList(tag(), "list");
        assertTrue(NbtCompat.getCompound(list, 5).isEmpty());
    }

    @Test
    void hasChecksTheType() {
        CompoundTag tag = tag();
        assertTrue(NbtCompat.hasCompound(tag, "compound"));
        assertFalse(NbtCompat.hasCompound(tag, "int"));
        assertFalse(NbtCompat.hasCompound(tag, "missing"));
        assertTrue(NbtCompat.hasList(tag, "list"));
        assertFalse(NbtCompat.hasList(tag, "compound"));
        assertTrue(NbtCompat.hasString(tag, "string"));
        assertFalse(NbtCompat.hasString(tag, "int"));
        assertTrue(NbtCompat.hasNumber(tag, "int"));
        assertTrue(NbtCompat.hasNumber(tag, "double"));
        assertTrue(NbtCompat.hasNumber(tag, "boolean"));
        assertFalse(NbtCompat.hasNumber(tag, "string"));
        assertFalse(NbtCompat.hasNumber(tag, "missing"));
    }

    @Test
    void uuidsSurviveSaveAndLoad() {
        UUID uuid = UUID.randomUUID();
        CompoundTag tag = new CompoundTag();
        NbtCompat.putUuid(tag, "UUID", uuid);
        assertEquals(uuid, NbtCompat.getUuid(tag, "UUID"));
    }

    /** The format of 1.20.1's {@code putUUID}, which older saves have: an array of four ints. */
    @Test
    void uuidsAreStoredAsFourInts() {
        UUID uuid = new UUID(0x0000000100000002L, 0x0000000300000004L);
        CompoundTag tag = new CompoundTag();
        NbtCompat.putUuid(tag, "UUID", uuid);
        IntArrayTag stored = assertInstanceOf(IntArrayTag.class, tag.get("UUID"));
        assertArrayEquals(new int[]{1, 2, 3, 4}, stored.getAsIntArray());

        CompoundTag old = new CompoundTag();
        old.putIntArray("UUID", new int[]{1, 2, 3, 4});
        assertEquals(uuid, NbtCompat.getUuid(old, "UUID"));
    }

    @Test
    void invalidUuidsReadAsNull() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("short", new int[]{1, 2});
        tag.put("string", StringTag.valueOf("not a uuid"));
        assertNull(NbtCompat.getUuid(tag, "short"));
        assertNull(NbtCompat.getUuid(tag, "string"));
    }
}
