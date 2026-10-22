package omtteam.omlib.client.gui;

import net.minecraft.entity.player.EntityPlayer;

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

    /** Number of GUIs on the stack; the open GUI is the newest one if it supports the back system itself. */
    public int size() {
        return guiList.size();
    }

    /**
     * Reopens the newest GUI on the stack, for screens that are not on the stack themselves (the camouflage menu). It
     * is taken off the stack, opening it puts it back.
     */
    public void openTopGui(EntityPlayer player) {
        ISupportsBackSystem gui = guiList.pop();
        if (gui.getGuiParameters() != null) {
            ClientUtil.openGuiFromParameters(player, gui.getGuiParameters());
        }
    }

    public void clearStack() {
        guiList = new Stack<>();
    }
}
