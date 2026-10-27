package omtteam.omlib.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A container with {@link LargeStackSlot}s (1.20.1+ {@code menu.LargeStackMenu}): it handles the clicks a slot cannot
 * limit by itself and syncs counts above one stack to the client.
 * <ul>
 *     <li>The hotbar swap (number keys) takes one stack out of a slot that holds more (vanilla would move it all).</li>
 *     <li>A click with the cursor on a slot holding more than one stack: the same item goes in up to the slot's limit
 *     (vanilla's 1.12 click handling would move items back onto the cursor), another item is not swapped in (the
 *     cursor would get all of the slot).</li>
 *     <li>Shift-click into a large slot fills it up to its limit ({@link #mergeItemStack}; vanilla stops at the
 *     item's max stack size).</li>
 *     <li>1.12 sends stack counts as a byte: a stack above one stack goes out as one stack carrying its real count in
 *     its NBT, which the client container takes back out. The stack a click returns is cut to one stack on both sides,
 *     as the client sends it with a byte count too. If the server still finds a click mismatched and resends the
 *     slots with plain counts ({@link #setCanCraft}), the large slots are sent again on the next tick.</li>
 * </ul>
 */
public abstract class LargeStackContainer extends Container {
    /** Key of the real count in a stack sent to the client; never stays on a stack. */
    private static final String COUNT_KEY = "OMLibCount";
    private static final int HOTBAR_SIZE = 9;

    private static boolean isLarge(ItemStack stack) {
        return stack.getCount() > stack.getMaxStackSize();
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        Slot slot = slotId >= 0 && slotId < inventorySlots.size() ? inventorySlots.get(slotId) : null;
        ItemStack result;
        if (slot instanceof LargeStackSlot && clickType == ClickType.SWAP && isLarge(slot.getStack())) {
            swapOut(slot, dragType, player);
            result = ItemStack.EMPTY;
        } else if (slot instanceof LargeStackSlot && clickType == ClickType.PICKUP && (dragType == 0 || dragType == 1)
                && slot.getHasStack() && !player.inventory.getItemStack().isEmpty() && slot.canTakeStack(player)
                && (isLarge(slot.getStack()) || sameItem(slot.getStack(), player.inventory.getItemStack()))) {
            result = slot.getStack().copy();
            putFromCursor(slot, dragType, player.inventory);
        } else {
            result = super.slotClick(slotId, dragType, clickType, player);
        }
        return isLarge(result) ? ItemHandlerHelper.copyStackWithSize(result, result.getMaxStackSize()) : result;
    }

    /** A hotbar swap on a slot holding more than one stack: one stack into an empty hotbar slot, nothing otherwise. */
    private static void swapOut(Slot slot, int button, EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        if (button < 0 || button >= HOTBAR_SIZE || !inventory.getStackInSlot(button).isEmpty()
                || !slot.canTakeStack(player)) {
            return;
        }
        ItemStack taken = slot.decrStackSize(slot.getStack().getMaxStackSize());
        inventory.setInventorySlotContents(button, taken);
        slot.onTake(player, taken);
    }

    /** The cursor onto a non-empty large slot: the same item up to the slot limit (all or one), never a swap. */
    private static void putFromCursor(Slot slot, int dragType, InventoryPlayer inventory) {
        ItemStack inSlot = slot.getStack();
        ItemStack cursor = inventory.getItemStack();
        if (!sameItem(inSlot, cursor) || !slot.isItemValid(cursor)) {
            return;
        }
        int moved = Math.min(dragType == 0 ? cursor.getCount() : 1, slot.getItemStackLimit(cursor) - inSlot.getCount());
        if (moved <= 0) {
            return;
        }
        cursor.shrink(moved);
        slot.putStack(ItemHandlerHelper.copyStackWithSize(inSlot, inSlot.getCount() + moved));
        slot.onSlotChanged();
    }

    private static boolean sameItem(ItemStack a, ItemStack b) {
        return ItemHandlerHelper.canItemStacksStack(a, b);
    }

    /** Vanilla's merge, but up to each slot's limit for the item (and through {@code putStack}, so handlers hear of it). */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean moved = false;
        int step = reverseDirection ? -1 : 1;
        int first = reverseDirection ? endIndex - 1 : startIndex;
        if (stack.isStackable()) {
            for (int i = first; !stack.isEmpty() && i >= startIndex && i < endIndex; i += step) {
                Slot slot = inventorySlots.get(i);
                ItemStack inSlot = slot.getStack();
                if (!inSlot.isEmpty() && sameItem(stack, inSlot)) {
                    int moving = Math.min(stack.getCount(), limit(slot, inSlot) - inSlot.getCount());
                    if (moving > 0) {
                        stack.shrink(moving);
                        slot.putStack(ItemHandlerHelper.copyStackWithSize(inSlot, inSlot.getCount() + moving));
                        slot.onSlotChanged();
                        moved = true;
                    }
                }
            }
        }
        if (!stack.isEmpty()) {
            for (int i = first; i >= startIndex && i < endIndex; i += step) {
                Slot slot = inventorySlots.get(i);
                if (slot.getStack().isEmpty() && slot.isItemValid(stack)) {
                    slot.putStack(stack.splitStack(Math.min(stack.getCount(), limit(slot, stack))));
                    slot.onSlotChanged();
                    moved = true;
                    break;
                }
            }
        }
        return moved;
    }

    private static int limit(Slot slot, ItemStack stack) {
        return slot instanceof LargeStackSlot ? slot.getItemStackLimit(stack)
                : Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
    }

    // ---- Sync ----

    /** {@code Container.addListener}, with the contents sent through {@link #withCountInTag}. */
    @Override
    public void addListener(IContainerListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already listening");
        }
        listeners.add(listener);
        NonNullList<ItemStack> sent = NonNullList.create();
        for (ItemStack stack : getInventory()) {
            sent.add(withCountInTag(stack));
        }
        listener.sendAllContents(this, sent);
        detectAndSendChanges();
    }

    /** {@code Container.detectAndSendChanges}, with the changed stacks sent through {@link #withCountInTag}. */
    @Override
    public void detectAndSendChanges() {
        for (int i = 0; i < inventorySlots.size(); i++) {
            ItemStack current = inventorySlots.get(i).getStack();
            ItemStack last = inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(last, current)) {
                boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(last, current);
                last = current.isEmpty() ? ItemStack.EMPTY : current.copy();
                inventoryItemStacks.set(i, last);
                if (clientStackChanged) {
                    ItemStack sent = withCountInTag(last);
                    for (IContainerListener listener : listeners) {
                        listener.sendSlotContents(this, i, sent);
                    }
                }
            }
        }
    }

    /**
     * The server found a click mismatched and resends all slots with plain (byte) counts: the large slots count as
     * unsent, so {@link #detectAndSendChanges} sends them again with their real counts.
     */
    @Override
    public void setCanCraft(EntityPlayer player, boolean canCraft) {
        super.setCanCraft(player, canCraft);
        if (!canCraft) {
            for (int i = 0; i < inventorySlots.size(); i++) {
                if (inventorySlots.get(i) instanceof LargeStackSlot && isLarge(inventorySlots.get(i).getStack())) {
                    inventoryItemStacks.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, FMLCommonHandler.instance().getEffectiveSide().isClient() ? restoreCount(stack) : stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setAll(List<ItemStack> stacks) {
        stacks.replaceAll(LargeStackContainer::restoreCount);
        super.setAll(stacks);
    }

    /** Server: a stack above one stack as one stack with its real count in its NBT. */
    private static ItemStack withCountInTag(ItemStack stack) {
        if (!isLarge(stack)) {
            return stack;
        }
        ItemStack sent = ItemHandlerHelper.copyStackWithSize(stack, stack.getMaxStackSize());
        if (!sent.hasTagCompound()) {
            sent.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        sent.getTagCompound().setInteger(COUNT_KEY, stack.getCount());
        return sent;
    }

    /** Client: takes the real count back out of a stack sent by {@link #withCountInTag}. */
    private static ItemStack restoreCount(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(COUNT_KEY, Constants.NBT.TAG_ANY_NUMERIC)) {
            return stack;
        }
        int count = tag.getInteger(COUNT_KEY);
        tag.removeTag(COUNT_KEY);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
        stack.setCount(Math.max(1, count));
        return stack;
    }
}
