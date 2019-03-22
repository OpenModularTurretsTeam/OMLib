package omtteam.omlib.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.gui.IHasTooltips;
import omtteam.omlib.api.permission.GlobalTrustRegister;
import omtteam.omlib.api.permission.ITrustedPlayersManager;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.api.permission.TrustedPlayersManagerGlobal;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.network.messages.*;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;
import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;
import static omtteam.omlib.util.player.PlayerUtil.isPlayerOwner;

public class TrustedPlayersGUI extends GuiScreen implements IHasTooltips {
    protected final int xSize = 176;
    protected final int ySize = 205;
    private final ITrustedPlayersManager tpm;
    private final EntityPlayer player;
    protected int guiLeft;
    protected int guiTop;
    private GuiTextField textFieldAddTrustedPlayer;
    private int mouseX;
    private int mouseY;
    private int waitForServerTrustedPlayers = -1;
    private boolean addedToSyncList;
    private int accessLevel = 0;
    private int trustedPlayerIndex;
    private boolean isGlobal;

    public TrustedPlayersGUI(InventoryPlayer inventoryPlayer, ITrustedPlayersManager trustedPlayersManager) {
        super();
        this.tpm = trustedPlayersManager;
        this.isGlobal = !tpm.hasTile();

        player = inventoryPlayer.player;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        if (!addedToSyncList) {
            if (tpm.hasTile()) {
                OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageOpenGUITile(tpm.getTile()));
            }
            addedToSyncList = true;
        }

        accessLevel = PlayerUtil.getPlayerAccessLevel(player, tpm).ordinal();
        if (accessLevel < 2) {
            player.closeScreen();
        }

        this.buttonList.clear();
        this.textFieldAddTrustedPlayer = null;
        this.buttonInit();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.mouseX = par1;
        this.mouseY = par2;

        if (accessLevel != PlayerUtil.getPlayerAccessLevel(player, tpm).ordinal() && !isPlayerOwner(player, tpm)) {
            accessLevel = PlayerUtil.getPlayerAccessLevel(player, tpm).ordinal();
            if (accessLevel != 0) {
                this.initGui();
            }
        } else if (!isPlayerOwner(player, tpm) && accessLevel < 2) {
            player.closeScreen();
            player.openGui(OMLib.instance, 0, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        ResourceLocation texture = new ResourceLocation(OMLibNames.Textures.trustedPlayers);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(texture);
        this.drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        if (accessLevel > 2) {
            fontRenderer.drawString(safeLocalize(OMLibNames.Localizations.GUI.ADD_TRUSTED_PLAYER) + ": ", guiLeft + 10, guiTop + 87, 0);

            if (this.tpm.getTrustedPlayers().size() == 0) {
                fontRenderer.drawString("\u00A7f" + safeLocalize(OMLibNames.Localizations.GUI.NO_TRUSTED_PLAYERS), guiLeft + 10, guiTop + 124, 0);
            } else {
                fontRenderer.drawString(tpm.getTrustedPlayers().get(trustedPlayerIndex).getName() + "'s " + safeLocalize(OMLibNames.Localizations.GUI.PERMISSIONS),
                                        guiLeft + 10, guiTop + 124, 0);
            }
        }

        super.drawScreen(par1, par2, par3);
        if (accessLevel > 2) {
            drawAccessMode();
        }
        if (textFieldAddTrustedPlayer != null) {
            textFieldAddTrustedPlayer.drawTextBox();
        }
        drawTooltips();
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        if (isMouseOverTextField(textFieldAddTrustedPlayer, i, j)) {
            if (textFieldAddTrustedPlayer.isFocused()) {
                textFieldAddTrustedPlayer.mouseClicked(i, j, k);
            } else {
                textFieldAddTrustedPlayer.setFocused(true);
            }
        } else {
            try {
                super.mouseClicked(i, j, k);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("EmptyCatchBlock")
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (textFieldAddTrustedPlayer == null || !textFieldAddTrustedPlayer.isFocused()) {
            try {
                super.keyTyped(typedChar, keyCode);
            } catch (IOException e) {

            }
        } else if (textFieldAddTrustedPlayer != null) {
            if (typedChar != 27) {
                textFieldAddTrustedPlayer.textboxKeyTyped(typedChar, keyCode);
            } else {
                textFieldAddTrustedPlayer.setFocused(false);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @ParametersAreNonnullByDefault
    protected void actionPerformed(GuiButton guibutton) {

        if (guibutton.id == 4) { //add trusted player
            if (PlayerUtil.isPlayerAdmin(player, tpm)) {
                if (!textFieldAddTrustedPlayer.getText().equals("") || !textFieldAddTrustedPlayer.getText().isEmpty()) {
                    sendChangeToServerAddTrusted();
                    textFieldAddTrustedPlayer.setText("");
                    waitForServerTrustedPlayers = 20;
                }
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
            }
        }

        if (guibutton.id == 5) { //remove trusted player

            if (this.tpm.getTrustedPlayers().size() <= trustedPlayerIndex) {
                return;
            }

            if (tpm.getTrustedPlayers().size() > 0) {
                if (this.tpm.getTrustedPlayers().get(
                        trustedPlayerIndex) != null && PlayerUtil.isPlayerAdmin(player, tpm)) {
                    sendChangeToServerRemoveTrusted();
                    tpm.removeTrustedPlayer(tpm.getTrustedPlayers().get(trustedPlayerIndex).getName());
                    textFieldAddTrustedPlayer.setText("");
                    this.trustedPlayerIndex = 0;
                    if (!player.getUniqueID().toString().equals(tpm.getOwner()) && (tpm.getTrustedPlayers().size() == 0 || PlayerUtil.isPlayerAdmin(player, tpm))) {
                        mc.displayGuiScreen(null);
                        return;
                    }
                    this.initGui();
                } else {
                    addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                }
            }
        }

        if (guibutton.id == 6) { //decrease index of trusted player list
            if ((this.trustedPlayerIndex - 1 >= 0)) {
                this.trustedPlayerIndex--;
                this.initGui();
            }
        }

        if (guibutton.id == 7) { //increase index of trusted player list
            if (!((this.trustedPlayerIndex + 1) > (tpm.getTrustedPlayers().size() - 1))) {
                this.trustedPlayerIndex++;
                this.initGui();
            }
        }

        if (this.tpm.getTrustedPlayers().size() <= trustedPlayerIndex) {
            return;
        }

        if (guibutton.id == 8) { // decrease permission level by 1
            if (PlayerUtil.isPlayerAdmin(player, tpm) && this.tpm.getTrustedPlayers().get(
                    trustedPlayerIndex) != null) {
                sendChangeToServerModifyPermissions(
                        this.tpm.getTrustedPlayers().get(trustedPlayerIndex).getName(), -1);
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
            }
        }

        if (guibutton.id == 9) { //increase permission level by 1
            if (PlayerUtil.isPlayerAdmin(player, tpm) && this.tpm.getTrustedPlayers().get(
                    trustedPlayerIndex) != null) {
                sendChangeToServerModifyPermissions(
                        this.tpm.getTrustedPlayers().get(trustedPlayerIndex).getName(), 1);
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
            }
        }
    }

    private void drawAccessMode() {
        if (tpm.getTrustedPlayers().size() > trustedPlayerIndex) {
            TrustedPlayer trustedPlayer = tpm.getTrustedPlayers().get(trustedPlayerIndex);
            if (trustedPlayer != null) {
                fontRenderer.drawString(trustedPlayer.getAccessLevel().ordinal() + "", guiLeft + 102, guiTop + 141, 0xFFFF00);
            }
        } else {
            fontRenderer.drawString("?", guiLeft + 102, guiTop + 140, 0);
        }
    }

    @Override
    public void drawTooltips() {
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        int tooltipToDraw = buttonList.stream().filter(GuiButton::isMouseOver).mapToInt(s -> s.id).sum();
        if (tooltipToDraw == 0) {
            tooltipToDraw = isMouseOverTextField(textFieldAddTrustedPlayer, mouseX, mouseY) ? 25 : 0;
        }
        if (tooltipToDraw == 0 && mouseX > guiLeft + 95 && mouseX < guiLeft + 115 && mouseY > guiTop + 135 && mouseY < guiTop + 155) {
            tooltipToDraw = 20;
        }

        ArrayList<String> tooltip = new ArrayList<>();
        switch (tooltipToDraw) {
            case 0:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.ADD_TRUSTED_PLAYER));
                break;
            case 1:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.REMOVE_TRUSTED_PLAYER));
                break;
            case 2:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.VIEW_NEXT_TRUSTED_PLAYER));
                break;
            case 3:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.VIEW_PREVIOUS_TRUSTED_PLAYER));
                break;
            case 4:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_DECREASE_ACCESS));
                break;
            case 5:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_INCREASE_ACCESS));
                break;
            case 20:
                if (tpm.getTrustedPlayers().size() > trustedPlayerIndex) {
                    TrustedPlayer trustedPlayer = tpm.getTrustedPlayers().get(trustedPlayerIndex);
                    if (trustedPlayer != null) {
                        switch (trustedPlayer.getAccessLevel()) {
                            case ADMIN:
                                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_CAN_ADMINISTER));
                                break;
                            case CHANGE_SETTINGS:
                                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_CAN_CHANGE_SETTINGS));
                                break;
                            case OPEN_GUI:
                                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_CAN_OPEN_GUI));
                                break;
                            case NONE:
                                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_NONE));
                                break;
                        }
                    }
                } else {
                    tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.INFO_ACCESS_LEVEL));
                }
                break;
            case 25:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TEXT_TRUSTED_PLAYER));
                break;
        }
        if (!tooltip.isEmpty())
            this.drawHoveringText(tooltip, guiLeft + mouseX - k, guiTop + mouseY - l, Minecraft.getMinecraft().fontRenderer);
    }

    protected void buttonInit() {
        if (PlayerUtil.isPlayerAdmin(player, tpm)) {
            textFieldAddTrustedPlayer = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 11, guiTop + 99, 100, 18);
            this.buttonList.add(new GuiButton(1, guiLeft + 114, guiTop + 98, 51, 20, "+"));
            this.buttonList.add(new GuiButton(2, guiLeft + 35, guiTop + 135, 30, 20, "-"));
            this.buttonList.add(new GuiButton(3, guiLeft + 70, guiTop + 135, 23, 20, "+"));
            this.buttonList.add(new GuiButton(4, guiLeft + 116, guiTop + 135, 23, 20, "-"));
            this.buttonList.add(new GuiButton(5, guiLeft + 10, guiTop + 135, 20, 20, "<<"));
            this.buttonList.add(new GuiButton(6, guiLeft + 145, guiTop + 135, 20, 20, ">>"));
        }
    }

    private void sendChangeToServerAddTrusted() {
        MessageAddTrustedPlayer message = new MessageAddTrustedPlayer(textFieldAddTrustedPlayer.getText(), tpm.getTile(), isGlobal, tpm.getOwner());

        OMLibNetworkingHandler.INSTANCE.sendToServer(message);
    }

    private void sendChangeToServerRemoveTrusted() {
        MessageRemoveTrustedPlayer message = new MessageRemoveTrustedPlayer(tpm.getTrustedPlayers().get(
                trustedPlayerIndex).getName(), tpm.getTile(), isGlobal, tpm.getOwner());

        OMLibNetworkingHandler.INSTANCE.sendToServer(message);
    }

    private void sendChangeToServerModifyPermissions(String player, Integer change) {
        MessageModifyPermissions message = new MessageModifyPermissions(player, tpm.getTile(), isGlobal, tpm.getOwner(), change);

        OMLibNetworkingHandler.INSTANCE.sendToServer(message);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (waitForServerTrustedPlayers >= 0 && this.tpm.getTrustedPlayers().size() > 0) {
            waitForServerTrustedPlayers = -1;
            this.trustedPlayerIndex = 0;
        } else if (waitForServerTrustedPlayers >= 0) {
            waitForServerTrustedPlayers--;
        }
        if (isGlobal) {
            Map.Entry<Player, TrustedPlayersManagerGlobal> entry = GlobalTrustRegister.instance.getEntryFromName(tpm.getOwner().getName());
            if (entry != null) {
                tpm.setTrustedPlayers(entry.getValue().getTrustedPlayers());
            }
        }
    }

    @Override
    public void onGuiClosed() {
        if (!isGlobal) {
            OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageCloseGUITile(tpm.getTile()));
        }
        super.onGuiClosed();
    }
}