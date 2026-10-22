package omtteam.omlib.client.gui;

import net.minecraft.client.gui.GuiTextField;

/**
 * Created by Keridos on 12/02/17.
 * This Class
 */
public interface IHasTooltips {
    void drawTooltips();

    default boolean isMouseOverTextField(GuiTextField guiTextField, int x, int y) {
        return (guiTextField != null && x >= guiTextField.x && x <= guiTextField.x + guiTextField.width
                && y >= guiTextField.y && y <= guiTextField.y + guiTextField.height);
    }
}
