package omtteam.omlib.client.gui;

import net.minecraft.client.gui.GuiTextField;

/**
 * Created by Keridos on 12/02/17.
 * This Class
 */
public interface IHasTooltips {
    void drawTooltips();

    default boolean isMouseOverTextField(GuiTextField guiTextField, int x, int y) {
        return (x >= guiTextField.xPosition && x <= guiTextField.xPosition + guiTextField.width
                && y >= guiTextField.yPosition && y <= guiTextField.yPosition + guiTextField.height);
    }
}
