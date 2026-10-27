package omtteam.omlib.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
//? if <1.21 {
/*import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.Slot;
import omtteam.omlib.compat.nbt.NbtCompat;

import java.util.List;
*///?}

/**
 * A menu with {@link LargeStackSlot}s: it handles the clicks a slot cannot limit by itself and, on 1.20.1, syncs
 * counts above one stack to the client.
 * <ul>
 *     <li>The hotbar swap (number keys) takes one stack out of a slot that holds more (vanilla would move it all).</li>
 *     <li>Shift-click into a large slot fills it up to its limit on every version (1.20.1's
 *     {@code moveItemStackTo} stops at the item's max stack size).</li>
 *     <li>1.20.1 sends stack counts as a byte: a stack above one stack goes out as one stack carrying its real count in
 *     its data, which the client menu takes back out. Later versions send counts in full.</li>
 * </ul>
 */
public abstract class LargeStackMenu extends AbstractContainerMenu {
    //? if <1.21 {
    /*/^* Key of the real count in a stack sent to the client; never stays on a stack. ^/
    private static final String COUNT_KEY = "OMLibCount";
    *///?}

    protected LargeStackMenu(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    private static boolean isLarge(ItemStack stack) {
        return stack.getCount() > stack.getMaxStackSize();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && slotId >= 0 && slotId < slots.size()
                && slots.get(slotId) instanceof LargeStackSlot slot && isLarge(slot.getItem())) {
            swapOut(slot, button, player);
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    /** A hotbar swap on a slot holding more than one stack: one stack into an empty hotbar slot, nothing otherwise. */
    private static void swapOut(LargeStackSlot slot, int button, Player player) {
        Inventory inventory = player.getInventory();
        boolean hotbarOrOffhand = button >= 0 && button < Inventory.getSelectionSize() || button == Inventory.SLOT_OFFHAND;
        if (!hotbarOrOffhand || !inventory.getItem(button).isEmpty() || !slot.mayPickup(player)) return;
        inventory.setItem(button, slot.safeTake(slot.getItem().getMaxStackSize(), Integer.MAX_VALUE, player));
    }

    //? if <1.21 {
    /*/^* 1.21's {@code moveItemStackTo}: it asks the slot for the limit of the moved item. ^/
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        if (stack.isStackable()) {
            for (int i = reverse ? end - 1 : start; !stack.isEmpty() && (reverse ? i >= start : i < end); i += reverse ? -1 : 1) {
                Slot slot = slots.get(i);
                ItemStack inSlot = slot.getItem();
                if (!inSlot.isEmpty() && ItemStack.isSameItemSameTags(stack, inSlot)) {
                    int limit = slot.getMaxStackSize(inSlot);
                    int total = inSlot.getCount() + stack.getCount();
                    if (total <= limit) {
                        stack.setCount(0);
                        inSlot.setCount(total);
                        slot.setChanged();
                        moved = true;
                    } else if (inSlot.getCount() < limit) {
                        stack.shrink(limit - inSlot.getCount());
                        inSlot.setCount(limit);
                        slot.setChanged();
                        moved = true;
                    }
                }
            }
        }
        if (!stack.isEmpty()) {
            for (int i = reverse ? end - 1 : start; reverse ? i >= start : i < end; i += reverse ? -1 : 1) {
                Slot slot = slots.get(i);
                if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                    slot.setByPlayer(stack.split(Math.min(stack.getCount(), slot.getMaxStackSize(stack))));
                    slot.setChanged();
                    moved = true;
                    break;
                }
            }
        }
        return moved;
    }

    @Override
    public void setSynchronizer(ContainerSynchronizer synchronizer) {
        super.setSynchronizer(new CountSynchronizer(synchronizer));
    }

    @Override
    public void setItem(int slot, int stateId, ItemStack stack) {
        super.setItem(slot, stateId, restoreCount(stack));
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        items.replaceAll(LargeStackMenu::restoreCount);
        super.initializeContents(stateId, items, carried);
    }

    /^* Server: a stack above one stack as one stack with its real count in its data. ^/
    private static ItemStack withCountInData(ItemStack stack) {
        if (!isLarge(stack)) return stack;
        ItemStack sent = stack.copyWithCount(stack.getMaxStackSize());
        sent.getOrCreateTag().putInt(COUNT_KEY, stack.getCount());
        return sent;
    }

    /^* Client: takes the real count back out of a stack sent by {@link #withCountInData}. ^/
    private static ItemStack restoreCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !NbtCompat.hasNumber(tag, COUNT_KEY)) return stack;
        int count = NbtCompat.getInt(tag, COUNT_KEY, stack.getCount());
        tag.remove(COUNT_KEY);
        if (tag.isEmpty()) stack.setTag(null);
        stack.setCount(count);
        return stack;
    }

    /^* Sends stacks above one stack through {@link #withCountInData}, everything else unchanged. ^/
    private record CountSynchronizer(ContainerSynchronizer delegate) implements ContainerSynchronizer {
        @Override
        public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> items, ItemStack carried, int[] data) {
            NonNullList<ItemStack> sent = NonNullList.withSize(items.size(), ItemStack.EMPTY);
            for (int i = 0; i < items.size(); i++) {
                sent.set(i, withCountInData(items.get(i)));
            }
            delegate.sendInitialData(menu, sent, withCountInData(carried), data);
        }

        @Override
        public void sendSlotChange(AbstractContainerMenu menu, int slot, ItemStack stack) {
            delegate.sendSlotChange(menu, slot, withCountInData(stack));
        }

        @Override
        public void sendCarriedChange(AbstractContainerMenu menu, ItemStack stack) {
            delegate.sendCarriedChange(menu, withCountInData(stack));
        }

        @Override
        public void sendDataChange(AbstractContainerMenu menu, int id, int value) {
            delegate.sendDataChange(menu, id, value);
        }
    }
    *///?}
}
