package omtteam.omlib.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A menu with {@link LargeStackSlot}s: it handles the clicks a slot cannot limit by itself.
 * <ul>
 *     <li>The hotbar swap (number keys) takes one stack out of a slot that holds more (vanilla would move it all).</li>
 *     <li>Shift-click into a large slot fills it up to its limit (vanilla's {@code moveItemStackTo} asks the slot for
 *     the limit of the moved item).</li>
 * </ul>
 */
public abstract class LargeStackMenu extends AbstractContainerMenu {

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

}
