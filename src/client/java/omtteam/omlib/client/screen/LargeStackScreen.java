package omtteam.omlib.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import omtteam.omlib.menu.LargeStackSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Container screen for a menu with {@link LargeStackSlot}s: it draws those slots itself, with a count that is scaled
 * down to the width of three digits (from four digits on; vanilla would draw it across the slot to the left), and
 * adds the count and the slot limit to the tooltip of an enlarged slot. Vanilla's slot rendering skips them, because
 * they look empty ({@link LargeStackSlot#setHidden}) while it runs; its hover highlight still lies on the slot, below
 * the item (vanilla draws it over the item).
 */
public abstract class LargeStackScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    /** Width a slot's count may take: three digits, as vanilla draws them. */
    private static final int COUNT_WIDTH = 18;
    /** Where vanilla's counts end, from the slot's corner. */
    private static final int COUNT_RIGHT = 17;

    protected LargeStackScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setLargeSlotsHidden(true);
        try {
            super.render(graphics, mouseX, mouseY, partialTick);
        } finally {
            setLargeSlotsHidden(false);
        }
        //? if <1.21.11 {
        // after vanilla's slots; the carried item and tooltips lie above by their depth
        renderLargeSlots(graphics, leftPos, topPos);
        //?}
    }

    //? if >=1.21.11 {
    /*/^* 1.21.11 draws in strata: the large slots go right after the other slots, below highlight and carried item. ^/
    @Override
    protected void renderSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderSlots(graphics, mouseX, mouseY);
        setLargeSlotsHidden(false);
        renderLargeSlots(graphics, 0, 0);
    }
    *///?}

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> lines = super.getTooltipFromContainerItem(stack);
        if (hoveredSlot instanceof LargeStackSlot slot) {
            int limit = slot.getMaxStackSize(stack);
            if (limit > stack.getMaxStackSize()) {
                lines = new ArrayList<>(lines);
                lines.add(Component.translatable("tooltip.omlib.slot_count", stack.getCount(), limit)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private void setLargeSlotsHidden(boolean hidden) {
        for (Slot slot : menu.slots) {
            if (slot instanceof LargeStackSlot large) large.setHidden(hidden);
        }
    }

    /** Draws the large slots' items; {@code x} / {@code y} is where the menu's slot positions start. */
    private void renderLargeSlots(GuiGraphics graphics, int x, int y) {
        for (Slot slot : menu.slots) {
            if (slot instanceof LargeStackSlot && slot.isActive() && slot.hasItem()) {
                renderLargeSlot(graphics, slot.getItem(), x + slot.x, y + slot.y);
            }
        }
    }

    private void renderLargeSlot(GuiGraphics graphics, ItemStack stack, int x, int y) {
        String count = stack.getCount() == 1 ? null : Integer.toString(stack.getCount());
        int width = count == null ? 0 : font.width(count);
        boolean small = width > COUNT_WIDTH;
        //? if >=1.21.11 {
        /*graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y, small ? "" : count);
        if (small) {
            float scale = (float) COUNT_WIDTH / width;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + COUNT_RIGHT, y + 17);
            graphics.pose().scale(scale, scale);
            graphics.drawString(font, count, -width, -font.lineHeight + 1, 0xFFFFFFFF, true);
            graphics.pose().popMatrix();
        }
        *///?} else {
        // the depths of vanilla's renderSlot (100) and renderItemDecorations (+200)
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 100.0F);
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y, small ? "" : count);
        if (small) {
            float scale = (float) COUNT_WIDTH / width;
            graphics.pose().translate(x + COUNT_RIGHT, y + 17, 200.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, count, -width, -font.lineHeight + 1, 0xFFFFFFFF, true);
        }
        graphics.pose().popPose();
        //?}
    }
}
