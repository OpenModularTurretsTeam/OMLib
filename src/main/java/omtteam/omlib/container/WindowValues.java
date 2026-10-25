package omtteam.omlib.container;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

import java.util.List;
import java.util.function.IntSupplier;

/**
 * Int values of a container that its client shows (energy, burn time), sent as window properties (1.20.1+
 * {@code ContainerData}). Window properties are 16 bit on 1.12, so value {@code i} goes as property {@code 2i} (low
 * 16 bits) and {@code 2i + 1} (high 16 bits). The container owns one and forwards its {@code addListener},
 * {@code detectAndSendChanges} and {@code updateProgressBar} to the methods of the same name.
 */
public final class WindowValues {
    private static final int HALF_BITS = 16;
    private static final int HALF_MASK = 0xFFFF;

    private final IntSupplier[] suppliers;
    /** Server: the values last sent to all listeners; client: the values received. */
    private final int[] values;

    /** @param values the current values, read on the server only */
    public WindowValues(IntSupplier... values) {
        this.suppliers = values.clone();
        this.values = new int[values.length];
    }

    /** Sends all current values to a new listener (the others get changes in {@link #detectAndSendChanges}). */
    public void addListener(Container container, IContainerListener listener) {
        for (int i = 0; i < suppliers.length; i++) {
            send(container, listener, i, suppliers[i].getAsInt());
        }
    }

    /** Sends the values that changed since the last call to all listeners. */
    public void detectAndSendChanges(Container container, List<IContainerListener> listeners) {
        for (int i = 0; i < suppliers.length; i++) {
            int current = suppliers[i].getAsInt();
            if (current != values[i]) {
                values[i] = current;
                for (IContainerListener listener : listeners) {
                    send(container, listener, i, current);
                }
            }
        }
    }

    /** Client: stores a received half; ids of other window properties are ignored. */
    public void updateProgressBar(int id, int data) {
        int index = id >> 1;
        if (id < 0 || index >= values.length) {
            return;
        }
        int half = data & HALF_MASK;
        values[index] = (id & 1) == 0
                ? (values[index] & ~HALF_MASK) | half
                : (values[index] & HALF_MASK) | half << HALF_BITS;
    }

    /** Value number {@code index}: on the server as last sent, on the client as last received. */
    public int get(int index) {
        return values[index];
    }

    private static void send(Container container, IContainerListener listener, int index, int value) {
        listener.sendWindowProperty(container, 2 * index, value & HALF_MASK);
        listener.sendWindowProperty(container, 2 * index + 1, value >>> HALF_BITS);
    }
}
