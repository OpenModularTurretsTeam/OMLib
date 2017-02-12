package omtteam.omlib.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import java.awt.*;
import java.util.ArrayList;


/**
 * Created by nico on 6/4/15.
 * Abstract class for all blocking UIs.
 */

public abstract class BlockingAbstractGuiContainer extends GuiContainer {
    public BlockingAbstractGuiContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    public abstract ArrayList<Rectangle> getBlockingAreas();
}
