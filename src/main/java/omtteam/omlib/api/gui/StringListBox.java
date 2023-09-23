package omtteam.omlib.api.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class StringListBox extends GuiButton {
    private static final int BASIC_X_OFFSET = 2;
    private static final int BASIC_Y_OFFSET = 2;
    private static final int SCROLL_WIDTH = 8;
    private static final int SCROLL_BUTTON_HEIGHT = 8;
    public int lineHeight;
    public boolean dragging;
    public int selectedItem;
    private final int fontColor = 0xff404040;
    private final int selectedColor = 0xff404040;
    private final int selectedFontColor = 0xffA0A0A0;
    private int scrollTop;
    private List<String> items;
    private int sliderHeight;
    private int sliderY;
    private int dragDelta;

    public StringListBox(int id, int left, int top, int width, int height, List<String> items) {
        super(id, left, top, width, height, "");
        this.items = items;
        scrollTop = 0;
        lineHeight = 0;
        sliderHeight = 0;
        dragging = false;
        dragDelta = 0;
        selectedItem = 0;
    }

    public void updateList(List<String> items) {
        this.items = items;
    }

    private void scrollTo(int pos) {
        scrollTop = pos;
        if (scrollTop < 0)
            scrollTop = 0;

        int max = lineHeight * items.size() + BASIC_Y_OFFSET - height;

        if (max < 0)
            max = 0;

        if (scrollTop > max)
            scrollTop = max;
    }

    public void scrollUp() {
        scrollTop -= 8;

        if (scrollTop < 0)
            scrollTop = 0;
    }

    public void scrollDown() {
        scrollTop += 8;
        int max = lineHeight * items.size() + BASIC_Y_OFFSET - height;

        if (max < 0)
            max = 0;

        if (scrollTop > max)
            scrollTop = max;
    }

    @Override
    public void drawButton(Minecraft minecraft, int cursorX, int cursorY, float i) {
        if (dragging) {
            int pos = (cursorY - y - SCROLL_BUTTON_HEIGHT - dragDelta) * (lineHeight * items.size() + BASIC_Y_OFFSET - height) /
                    Math.max(height - 2 * SCROLL_BUTTON_HEIGHT - sliderHeight, 1);

            scrollTo(pos);
        }

        FontRenderer fontRenderer = minecraft.fontRenderer;
        if (lineHeight == 0) {
            lineHeight = fontRenderer.FONT_HEIGHT + 2;
            if (scrollTop == 0) {
                int rowsPerHeight = height / lineHeight;

                if (selectedItem >= rowsPerHeight)
                    scrollTop = (selectedItem + 1) * lineHeight + BASIC_Y_OFFSET - height;
            }
            float scale = height / ((float) lineHeight * items.size() + BASIC_Y_OFFSET);

            if (scale > 1)
                scale = 1;

            sliderHeight = Math.round(scale * (height - 2 * SCROLL_BUTTON_HEIGHT));

            if (sliderHeight < 4)
                sliderHeight = 4;
        }
        // Slider
        int sliderX = x + width - SCROLL_WIDTH + 1;
        sliderY = y
                + SCROLL_BUTTON_HEIGHT
                + ((height - 2 * SCROLL_BUTTON_HEIGHT - sliderHeight) * scrollTop)
                / (lineHeight * items.size() + BASIC_Y_OFFSET - height);

        //drawSlider(tessellator, sliderX + SCROLL_WIDTH - 1, sliderY + sliderHeight - 1, zLevel, sliderHeight)

        int rowTop = BASIC_Y_OFFSET;
        GL11.glPushMatrix();
        // Background
        drawRect(this.x, this.y, x + this.width, y + this.height, 0xff101010);
        // Slider Buttons
        drawRect(this.x + this.width - SCROLL_WIDTH, this.y, this.x + this.width, this.y + SCROLL_BUTTON_HEIGHT, 0xff808080);
        drawRect(this.x + this.width - SCROLL_WIDTH, this.y + this.height - SCROLL_BUTTON_HEIGHT, this.x + this.width, this.y + this.height, 0xff808080);
        // Slider
        drawRect(sliderX - 1, sliderY, sliderX + SCROLL_WIDTH - 1, sliderY + sliderHeight, 0xff252525);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft mc = FMLClientHandler.instance().getClient();
        ScaledResolution scaler = new ScaledResolution(mc);
        GL11.glScissor(x * scaler.getScaleFactor(), mc.displayHeight - (y + height) * scaler.getScaleFactor(), (width - SCROLL_WIDTH) * scaler.getScaleFactor(), height * scaler.getScaleFactor());
        if (items.size() > 0) {
            selectedItem = Math.max(0, selectedItem);
            for (String row : items) {
                if (row.equals(items.get(selectedItem))) {
                    drawRect(x, y + rowTop - scrollTop - 1, x + width - SCROLL_WIDTH, y + rowTop - scrollTop + lineHeight - 1, selectedColor);
                    fontRenderer.drawString(row, x + BASIC_X_OFFSET, y + rowTop - scrollTop, selectedFontColor);
                } else {
                    fontRenderer.drawString(row, x + BASIC_X_OFFSET, y + rowTop - scrollTop, fontColor);
                }
                rowTop += lineHeight;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    private void setCurrent(int targetY) {
        if (lineHeight == 0)
            return;

        int itemIndex = (targetY - BASIC_Y_OFFSET - y + scrollTop) / lineHeight;
        if (itemIndex >= items.size())
            itemIndex = items.size() - 1;
        selectedItem = itemIndex;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean mousePressed(Minecraft minecraft, int targetX, int targetY) {
        if (super.mousePressed(minecraft, targetX, targetY)) {
            if (targetX > x + width - SCROLL_WIDTH) {// scroll click

                if (targetY - y < SCROLL_BUTTON_HEIGHT)
                    scrollUp();
                else if (height + y - targetY < SCROLL_BUTTON_HEIGHT)
                    scrollDown();
                else if (targetY >= sliderY && targetY <= sliderY + sliderHeight) {
                    dragging = true;
                    dragDelta = targetY - sliderY;
                }
            } else {
                setCurrent(targetY);

                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public void mouseReleased(int i, int j) {
        super.mouseReleased(i, j);
        dragging = false;
    }
}