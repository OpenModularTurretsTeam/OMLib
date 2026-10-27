package omtteam.omlib.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import omtteam.omlib.container.LargeStackSlot;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Container GUI for a container with {@link LargeStackSlot}s (1.20.1+ {@code client.screen.LargeStackScreen}): it
 * draws those slots itself, with a count that is scaled down to the width of three digits (from four digits on;
 * vanilla would draw it across the slot to the left), and adds the count and the slot limit to the tooltip of an
 * enlarged slot. Vanilla's slot rendering skips them, because they look empty ({@link LargeStackSlot#setHidden})
 * while it runs; its hover highlight still lies on the slot, below the item (vanilla draws it over the item).
 * <p>
 * The large slots are drawn in {@link #drawGuiContainerForegroundLayer}, below the carried item and the tooltips:
 * a subclass that overrides it calls {@code super} before anything that reads the slots.
 * It is a {@link BlockingAbstractGuiContainer} without blocking areas, so a subclass can name some for JEI.
 */
public abstract class LargeStackGui extends BlockingAbstractGuiContainer {
    /** Width a slot's count may take: three digits, as vanilla draws them. */
    private static final int COUNT_WIDTH = 18;
    /** Where vanilla's counts end, from the slot's corner. */
    private static final int COUNT_RIGHT = 17;
    /** Vanilla's z level of slot items and their counts. */
    private static final float SLOT_Z = 100.0F;

    protected LargeStackGui(Container container) {
        super(container);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        setLargeSlotsHidden(true);
        try {
            super.drawScreen(mouseX, mouseY, partialTicks);
        } finally {
            setLargeSlotsHidden(false);
        }
    }

    /** After vanilla's slots (the matrix starts at the GUI's corner): shows and draws the large slots. */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        setLargeSlotsHidden(false);
        RenderHelper.enableGUIStandardItemLighting();
        for (Slot slot : inventorySlots.inventorySlots) {
            if (slot instanceof LargeStackSlot && slot.isEnabled() && slot.getHasStack()) {
                renderLargeSlot(slot.getStack(), slot.xPos, slot.yPos);
            }
        }
        RenderHelper.disableStandardItemLighting();
    }

    @Nonnull
    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        List<String> lines = super.getItemToolTip(stack);
        Slot slot = getSlotUnderMouse();
        if (slot instanceof LargeStackSlot && slot.getStack() == stack) {
            int limit = slot.getItemStackLimit(stack);
            if (limit > stack.getMaxStackSize()) {
                lines = new ArrayList<>(lines);
                lines.add(TextFormatting.GRAY + I18n.format("tooltip.omlib.slot_count", stack.getCount(), limit));
            }
        }
        return lines;
    }

    @Override
    public List<Rectangle> getBlockingAreas() {
        return new ArrayList<>();
    }

    private void setLargeSlotsHidden(boolean hidden) {
        for (Slot slot : inventorySlots.inventorySlots) {
            if (slot instanceof LargeStackSlot) {
                ((LargeStackSlot) slot).setHidden(hidden);
            }
        }
    }

    /** Vanilla's {@code drawSlot} for one item, with the count scaled down from four digits on. */
    private void renderLargeSlot(ItemStack stack, int x, int y) {
        String count = stack.getCount() == 1 ? null : Integer.toString(stack.getCount());
        int width = count == null ? 0 : fontRenderer.getStringWidth(count);
        boolean small = width > COUNT_WIDTH;
        zLevel = SLOT_Z;
        itemRender.zLevel = SLOT_Z;
        GlStateManager.enableDepth();
        itemRender.renderItemAndEffectIntoGUI(mc.player, stack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, small ? "" : count);
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
        if (small) {
            float scale = (float) COUNT_WIDTH / width;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + COUNT_RIGHT, y + 17, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            fontRenderer.drawStringWithShadow(count, -width, -fontRenderer.FONT_HEIGHT + 1, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }
}
