package omtteam.omlib.container;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowValuesTest {
    /** Passes window properties to a client copy the way the packet does: as shorts. */
    private static final class Client implements IContainerListener {
        final WindowValues values = new WindowValues(() -> 0, () -> 0);
        final List<Integer> ids = new ArrayList<>();

        @Override
        public void sendWindowProperty(Container container, int id, int value) {
            ids.add(id);
            values.updateProgressBar(id, (short) value);
        }

        @Override
        public void sendAllContents(Container container, NonNullList<ItemStack> items) {
        }

        @Override
        public void sendSlotContents(Container container, int slot, ItemStack stack) {
        }

        @Override
        public void sendAllWindowProperties(Container container, IInventory inventory) {
        }
    }

    private final int[] server = {0, 0};
    private final WindowValues values = new WindowValues(() -> server[0], () -> server[1]);

    @Test
    void sendsAllValuesInTwoHalvesToANewListener() {
        server[0] = 123_456;
        server[1] = -7;
        Client client = new Client();
        values.addListener(null, client);
        assertEquals(123_456, client.values.get(0));
        assertEquals(-7, client.values.get(1));
        assertEquals(4, client.ids.size());
    }

    @Test
    void sendsOnlyChangedValues() {
        Client client = new Client();
        List<IContainerListener> listeners = Collections.singletonList(client);
        server[0] = 50_000;
        values.detectAndSendChanges(null, listeners);
        assertEquals(2, client.ids.size()); // value 1 is still 0
        client.ids.clear();
        values.detectAndSendChanges(null, listeners);
        assertEquals(0, client.ids.size());
        server[1] = Integer.MAX_VALUE;
        values.detectAndSendChanges(null, listeners);
        assertEquals(Integer.MAX_VALUE, client.values.get(1));
        assertEquals(50_000, client.values.get(0));
        assertEquals(Integer.MAX_VALUE, values.get(1));
    }

    @Test
    void ignoresOtherWindowProperties() {
        values.updateProgressBar(4, 1);
        values.updateProgressBar(-1, 1);
        assertEquals(0, values.get(0));
        assertEquals(0, values.get(1));
    }
}
