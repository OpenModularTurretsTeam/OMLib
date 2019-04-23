package omtteam.omlib.handler;

import net.minecraft.entity.player.EntityPlayer;
import omtteam.omlib.api.gui.ISupportsBackSystem;
import omtteam.omlib.util.ClientUtil;

import java.util.Stack;

public class GUIBackSystem {
    private static GUIBackSystem instance;
    private Stack<ISupportsBackSystem> guiList = new Stack<>();

    private GUIBackSystem() {
    }

    public static GUIBackSystem getInstance() {
        if (instance == null) {
            instance = new GUIBackSystem();
        }
        return instance;
    }

    public void addGuiToStack(ISupportsBackSystem gui) {
        guiList.push(gui);
    }

    public void openLastGui(EntityPlayer player) {
        guiList.pop();
        ISupportsBackSystem gui = guiList.pop();
        if (gui.getGuiParameters() != null) {
            ClientUtil.openGuiFromParameters(player, gui.getGuiParameters());
        }
    }

    public void clearStack() {
        guiList = new Stack<>();
    }
}
