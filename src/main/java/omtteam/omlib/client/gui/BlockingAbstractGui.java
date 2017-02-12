package omtteam.omlib.client.gui;

import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;


/**
 * Created by nico on 6/4/15.
 * Abstract class for all blocking UIs.
 */

public abstract class BlockingAbstractGui extends Gui {
    public BlockingAbstractGui() {
        super();
    }

    public abstract ArrayList<Rectangle> getBlockingAreas();
}
