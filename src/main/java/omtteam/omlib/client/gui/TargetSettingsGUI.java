package omtteam.omlib.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.gui.GuiParameters;
import omtteam.omlib.api.gui.IHasTooltips;
import omtteam.omlib.api.gui.ISupportsBackSystem;
import omtteam.omlib.api.network.ISyncableTE;
import omtteam.omlib.api.tile.IHasTargetingSettings;
import omtteam.omlib.handler.GUIBackSystem;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.network.messages.MessageCloseGUITile;
import omtteam.omlib.network.messages.MessageOpenGUITile;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.player.PlayerUtil;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

import static omtteam.omlib.util.GeneralUtil.getColoredBooleanLocalizationYesNo;
import static omtteam.omlib.util.GeneralUtil.safeLocalize;
import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;
import static omtteam.omlib.util.player.PlayerUtil.isPlayerOwner;

public class TargetSettingsGUI extends GuiScreen implements IHasTooltips, ISupportsBackSystem {
    protected final int xSize = 176;
    protected final int ySize = 205;
    private final EntityPlayer player;
    private final IHasTargetingSettings targetHandler;
    private final ISyncableTE syncHandler;
    protected int guiLeft;
    protected int guiTop;
    private int mouseX;
    private int mouseY;
    private boolean addedToSyncList;
    private int accessLevel = 0;

    public TargetSettingsGUI(InventoryPlayer inventoryPlayer, Object input) {
        super();
        targetHandler = (IHasTargetingSettings) input;

        if (input instanceof ISyncableTE) {
            syncHandler = (ISyncableTE) input;
        } else {
            syncHandler = null;
        }

        player = inventoryPlayer.player;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void buttonInit() {

        if (PlayerUtil.canPlayerChangeSetting(player, targetHandler)) {
            String mobsButton = safeLocalize(OMLibNames.Localizations.GUI.ATTACK_MOBS) + ": " + (getColoredBooleanLocalizationYesNo(targetHandler.getTargetingSettings().isTargetMobs()));
            String neutralsButton = safeLocalize(OMLibNames.Localizations.GUI.ATTACK_NEUTRALS) + ": " + (getColoredBooleanLocalizationYesNo(targetHandler.getTargetingSettings().isTargetPassive()));
            String playersButton = safeLocalize(OMLibNames.Localizations.GUI.ATTACK_PLAYERS) + ": " + (getColoredBooleanLocalizationYesNo(targetHandler.getTargetingSettings().isTargetPlayers()));
            this.buttonList.add(new GuiButton(0, guiLeft + 10, guiTop + 20, 155, 20, mobsButton));
            this.buttonList.add(new GuiButton(1, guiLeft + 10, guiTop + 40, 155, 20, neutralsButton));
            this.buttonList.add(new GuiButton(2, guiLeft + 10, guiTop + 60, 155, 20, playersButton));
        }

        this.buttonList.add(new GuiButton(3, guiLeft + 185, guiTop + 20, 80, 20, safeLocalize(OMLibNames.Localizations.GUI.BACK)));
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        if (!addedToSyncList && syncHandler != null) {
            OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageOpenGUITile(syncHandler.getTE()));
            addedToSyncList = true;
        }

        accessLevel = PlayerUtil.getPlayerAccessLevel(player, targetHandler).ordinal();
        if (accessLevel < 2) {
            player.closeScreen();
        }

        this.buttonList.clear();
        this.buttonInit();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.mouseX = par1;
        this.mouseY = par2;

        this.drawDefaultBackground();

        if (accessLevel != PlayerUtil.getPlayerAccessLevel(player, targetHandler).ordinal() && !isPlayerOwner(player, targetHandler)) {
            accessLevel = PlayerUtil.getPlayerAccessLevel(player, targetHandler).ordinal();
            if (accessLevel != 0) {
                this.initGui();
            }
        }
        ResourceLocation texture = (new ResourceLocation(OMLibNames.Textures.targetingSettings));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(texture);
        this.drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(safeLocalize(OMLibNames.Localizations.GUI.TARGETING_OPTIONS) + ": ", guiLeft + 10, guiTop + 9, 0);
        buttonList.get(0).displayString = safeLocalize(OMLibNames.Localizations.GUI.ATTACK_MOBS) + ": " + (getColoredBooleanLocalizationYesNo(targetHandler.getTargetingSettings().isTargetMobs()));
        buttonList.get(1).displayString = safeLocalize(OMLibNames.Localizations.GUI.ATTACK_NEUTRALS) + ": " + (getColoredBooleanLocalizationYesNo(targetHandler.getTargetingSettings().isTargetPassive()));
        buttonList.get(2).displayString = safeLocalize(OMLibNames.Localizations.GUI.ATTACK_PLAYERS) + ": " + (getColoredBooleanLocalizationYesNo(targetHandler.getTargetingSettings().isTargetPlayers()));

        super.drawScreen(par1, par2, par3);
        drawTooltips();
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) { //change Attack Mobs
            if (PlayerUtil.canPlayerChangeSetting(player, targetHandler)) {
                targetHandler.getTargetingSettings().setTargetMobs(!targetHandler.getTargetingSettings().isTargetMobs());
                targetHandler.informUpdate();
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize(OMLibNames.Localizations.Text.STATUS_PERMISSION)));
            }
        }

        if (guibutton.id == 1) { //change Attack Neutrals
            if (PlayerUtil.canPlayerChangeSetting(player, targetHandler)) {
                targetHandler.getTargetingSettings().setTargetPassive(!targetHandler.getTargetingSettings().isTargetPassive());
                targetHandler.informUpdate();
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize(OMLibNames.Localizations.Text.STATUS_PERMISSION)));
            }
        }

        if (guibutton.id == 2) { // change Attack Players
            if (PlayerUtil.canPlayerChangeSetting(player, targetHandler)) {
                targetHandler.getTargetingSettings().setTargetPlayers(!targetHandler.getTargetingSettings().isTargetPlayers());
                targetHandler.informUpdate();
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize(OMLibNames.Localizations.Text.STATUS_PERMISSION)));
            }
        }
        if (guibutton.id == 3) { //back button
            GUIBackSystem.getInstance().openLastGui(player);
        }
    }

    @Override
    public void drawTooltips() {
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        int tooltipToDraw = buttonList.stream().filter(GuiButton::isMouseOver).mapToInt(s -> s.id).sum();
        ArrayList<String> tooltip = new ArrayList<>();
        switch (tooltipToDraw) {
            case 0:
                if (buttonList.get(0).isMouseOver()) {
                    tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TARGET_MOBS));
                }
                break;
            case 1:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TARGET_NEUTRALS));
                break;
            case 2:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TARGET_PLAYERS));
                break;
            case 3:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TRUSTED_PLAYER_GUI));
                break;
            default:
        }
        if (!tooltip.isEmpty())
            this.drawHoveringText(tooltip, guiLeft + mouseX - k, guiTop + mouseY - l, Minecraft.getMinecraft().fontRenderer);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageCloseGUITile(syncHandler.getTE()));
        super.onGuiClosed();
    }

    @Override
    @Nullable
    public GuiParameters getGuiParameters() {
        return new GuiParameters(OMLib.instance, 1, player.getEntityWorld(),
                                 syncHandler.getTE().getPos().getX(),
                                 syncHandler.getTE().getPos().getY(),
                                 syncHandler.getTE().getPos().getZ());
    }
}
