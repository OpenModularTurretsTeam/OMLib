package omtteam.omlib.menu;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongContainerDataTest {
    /** What the client gets: vanilla sends every container data value as a (signed) short. */
    private static ContainerData synced(ContainerData server) {
        SimpleContainerData client = new SimpleContainerData(server.getCount());
        for (int i = 0; i < server.getCount(); i++) {
            client.set(i, (short) server.get(i));
        }
        return client;
    }

    private static long roundTrip(long value) {
        return LongContainerData.read(synced(new LongContainerData(() -> value)), 0);
    }

    @Test
    void twoSlotsPerValue() {
        assertEquals(6, LongContainerData.size(3));
        assertEquals(6, new LongContainerData(() -> 1, () -> 2, () -> 3).getCount());
    }

    @Test
    void valuesSurviveTheSyncAsShorts() {
        for (long value : new long[]{0, 1, 0x7FFF, 0x8000, 0xFFFF, 0x10000, 123_456_789, Integer.MAX_VALUE}) {
            assertEquals(value, roundTrip(value), Long.toString(value));
        }
    }

    @Test
    void valuesAreClampedToTheIntRange() {
        assertEquals(Integer.MAX_VALUE, roundTrip(Long.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, roundTrip(Integer.MAX_VALUE + 1L));
        assertEquals(0, roundTrip(-5));
    }

    @Test
    void eachValueKeepsItsOwnSlots() {
        ContainerData client = synced(new LongContainerData(() -> 70_000, () -> 3, () -> 1_000_000));
        assertEquals(70_000, LongContainerData.read(client, 0));
        assertEquals(3, LongContainerData.read(client, 1));
        assertEquals(1_000_000, LongContainerData.read(client, 2));
    }

    @Test
    void theCurrentValueIsReadOnEveryGet() {
        long[] value = {10};
        LongContainerData data = new LongContainerData(() -> value[0]);
        value[0] = 0x20005;
        assertEquals(0x20005, LongContainerData.read(data, 0));
    }

    @Test
    void setIsIgnored() {
        LongContainerData data = new LongContainerData(() -> 12);
        data.set(0, 99);
        assertEquals(12, LongContainerData.read(data, 0));
    }
}
