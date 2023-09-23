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
import omtteam.omlib.api.gui.GuiParameters;
import omtteam.omlib.api.gui.IHasTooltips;
import omtteam.omlib.api.gui.ISupportsBackSystem;
import omtteam.omlib.api.gui.StringListBox;
import omtteam.omlib.api.permission.GlobalTrustRegister;
import omtteam.omlib.api.permission.ITrustedPlayersManager;
import omtteam.omlib.api.permission.TrustedPlayer;
import omtteam.omlib.api.permission.TrustedPlayersManager;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.handler.GUIBackSystem;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.network.messages.*;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;
import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;
import static omtteam.omlib.util.player.PlayerUtil.isPlayerOwner;

public class TrustedPlayersGUI extends GuiScreen implements IHasTooltips, ISupportsBackSystem {
    protected final int xSize = 256;
    protected final int ySize = 256;
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
    private final boolean isGlobal;
    private final List<String> trustedPlayers = new ArrayList<>();
    private StringListBox listBox;

    public TrustedPlayersGUI(InventoryPlayer inventoryPlayer, IHasTrustManager tile) {
        super();
        this.tpm = tile.getTrustManager();
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
        accessLevel = PlayerUtil.getPlayerAccessLevel(player, (IHasTrustManager) tpm.getTile()).ordinal();
        if (accessLevel < 2) {
            player.closeScreen();
        }

        for (TrustedPlayer trustedPlayer : tpm.getTrustedPlayers()) {
            this.trustedPlayers.add(trustedPlayer.getName() + ", " + trustedPlayer.getAccessLevel().getLocalizedName());
        }

        this.buttonList.clear();
        this.textFieldAddTrustedPlayer = null;
        this.buttonInit();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        this.mouseX = par1;
        this.mouseY = par2;

        this.drawDefaultBackground();

        if (accessLevel != PlayerUtil.getPlayerAccessLevel(player, (IHasTrustManager) tpm.getTile()).ordinal() && !isPlayerOwner(player, (IHasTrustManager) tpm.getTile())) {
            accessLevel = PlayerUtil.getPlayerAccessLevel(player, (IHasTrustManager) tpm.getTile()).ordinal();
        }
        if (!isPlayerOwner(player, (IHasTrustManager) tpm.getTile()) && accessLevel < 3) {
            player.closeScreen();
        }
        ResourceLocation texture = new ResourceLocation(OMLibNames.Textures.trustedPlayers);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(texture);
        this.drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(safeLocalize(OMLibNames.Localizations.GUI.ADD_TRUSTED_PLAYER) + ": ", guiLeft + 10, guiTop + 220, 0);
        fontRenderer.drawString(safeLocalize(OMLibNames.Localizations.GUI.PERMISSIONS), guiLeft + 186, guiTop + 220, 0);

        super.drawScreen(par1, par2, par3);
        if (textFieldAddTrustedPlayer != null) {
            textFieldAddTrustedPlayer.drawTextBox();
        }
        drawTooltips();
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
            case 1:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.ADD_TRUSTED_PLAYER));
                break;
            case 2:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.REMOVE_TRUSTED_PLAYER));
                break;
            case 3:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_DECREASE_ACCESS));
                break;
            case 4:
                tooltip.add(safeLocalize(OMLibNames.Localizations.Tooltip.TP_INCREASE_ACCESS));
                break;
            case 20:
                if (listBox.selectedItem >= 0 && tpm.getTrustedPlayers().size() > listBox.selectedItem) {
                    TrustedPlayer trustedPlayer = tpm.getTrustedPlayers().get(listBox.selectedItem);
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
        if (PlayerUtil.isPlayerAdmin(player, (IHasTrustManager) tpm.getTile())) {

            textFieldAddTrustedPlayer = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 11, guiTop + 231, 100, 18);
            this.listBox = new StringListBox(5, guiLeft + 5, guiTop + 5, 246, 212, trustedPlayers);
            this.buttonList.add(new GuiButton(1, guiLeft + 114, guiTop + 230, 33, 20, "+"));
            this.buttonList.add(new GuiButton(2, guiLeft + 148, guiTop + 230, 33, 20, "-"));
            this.buttonList.add(new GuiButton(3, guiLeft + 185, guiTop + 230, 23, 20, "-"));
            this.buttonList.add(new GuiButton(4, guiLeft + 226, guiTop + 230, 23, 20, "+"));
            this.buttonList.add(new GuiButton(6, guiLeft + 261, guiTop + 10, 80, 20, safeLocalize(OMLibNames.Localizations.GUI.BACK)));
            this.buttonList.add(listBox);
        }
    }

    public void updateList() {
        this.trustedPlayers.clear();
        for (TrustedPlayer trustedPlayer : tpm.getTrustedPlayers()) {
            this.trustedPlayers.add(trustedPlayer.getName() + ", " + trustedPlayer.getAccessLevel().getLocalizedName());
        }
        if (this.listBox != null) {
            this.listBox.updateList(trustedPlayers);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (waitForServerTrustedPlayers >= 0 && this.tpm.getTrustedPlayers().size() > 0) {
            waitForServerTrustedPlayers = -1;
        } else if (waitForServerTrustedPlayers >= 0) {
            waitForServerTrustedPlayers--;
        }
        updateList();
        if (isGlobal) {
            Map.Entry<Player, TrustedPlayersManager> entry = GlobalTrustRegister.instance.getEntryFromName(tpm.getOwner().getName());
            if (entry != null) {
                tpm.setTrustedPlayers(entry.getValue().getTrustedPlayers());
            }
        }
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

        if (guibutton.id == 1) { //add trusted player
            if (PlayerUtil.isPlayerAdmin(player, (IHasTrustManager) tpm.getTile())) {
                if (!textFieldAddTrustedPlayer.getText().equals("") || !textFieldAddTrustedPlayer.getText().isEmpty()) {
                    sendChangeToServerAddTrusted();
                    textFieldAddTrustedPlayer.setText("");
                    waitForServerTrustedPlayers = 20;
                }
            } else {
                addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
            }
        }

        if (guibutton.id == 2) { //remove trusted player
            if (this.tpm.getTrustedPlayers().size() < listBox.selectedItem) {
                return;
            }

            if (tpm.getTrustedPlayers().size() > 0) {
                if (this.tpm.getTrustedPlayers().get(
                        listBox.selectedItem) != null && PlayerUtil.isPlayerAdmin(player, (IHasTrustManager) tpm.getTile())) {
                    sendChangeToServerRemoveTrusted();
                    tpm.removeTrustedPlayer(tpm.getTrustedPlayers().get(listBox.selectedItem).getName());
                    if (!player.getUniqueID().equals(tpm.getOwner().getUuid()) && (tpm.getTrustedPlayers().size() == 0 || PlayerUtil.isPlayerAdmin(player, (IHasTrustManager) tpm.getTile()))) {
                        mc.displayGuiScreen(null);
                        return;
                    }
                    this.initGui();
                } else {
                    addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                }
            }
        }

        if (guibutton.id == 3) { // decrease permission level by 1
            if (this.tpm.getTrustedPlayers().size() < listBox.selectedItem) {
                return;
            }

            if (tpm.getTrustedPlayers().size() > 0) {
                if (PlayerUtil.isPlayerAdmin(player, (IHasTrustManager) tpm.getTile()) && this.tpm.getTrustedPlayers().get(
                        listBox.selectedItem) != null) {
                    sendChangeToServerModifyPermissions(
                            this.tpm.getTrustedPlayers().get(listBox.selectedItem).getName(), -1);
                } else {
                    addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                }
            }
        }

        if (guibutton.id == 4) { //increase permission level by 1
            if (this.tpm.getTrustedPlayers().size() < listBox.selectedItem) {
                return;
            }

            if (tpm.getTrustedPlayers().size() > 0) {
                if (PlayerUtil.isPlayerAdmin(player, (IHasTrustManager) tpm.getTile()) && this.tpm.getTrustedPlayers().get(
                        listBox.selectedItem) != null) {
                    sendChangeToServerModifyPermissions(
                            this.tpm.getTrustedPlayers().get(listBox.selectedItem).getName(), 1);
                } else {
                    addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                }
            }
        }
        if (guibutton.id == 6) { //back button
            GUIBackSystem.getInstance().openLastGui(player);
        }
    }

    private void sendChangeToServerAddTrusted() {
        MessageAddTrustedPlayer message = new MessageAddTrustedPlayer(textFieldAddTrustedPlayer.getText(), tpm.getTile(), isGlobal, tpm.getOwner());

        OMLibNetworkingHandler.INSTANCE.sendToServer(message);
    }

    private void sendChangeToServerRemoveTrusted() {
        MessageRemoveTrustedPlayer message = new MessageRemoveTrustedPlayer(tpm.getTrustedPlayers().get(
                listBox.selectedItem).getName(), tpm.getTile(), isGlobal, tpm.getOwner());

        OMLibNetworkingHandler.INSTANCE.sendToServer(message);
    }

    private void sendChangeToServerModifyPermissions(String player, Integer change) {
        MessageModifyPermissions message = new MessageModifyPermissions(player, tpm.getTile(), isGlobal, tpm.getOwner(), change);

        OMLibNetworkingHandler.INSTANCE.sendToServer(message);
    }

    @Override
    public void onGuiClosed() {
        if (!isGlobal) {
            OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageCloseGUITile(tpm.getTile()));
        }
        super.onGuiClosed();
    }

    @Override
    @Nullable
    public GuiParameters getGuiParameters() {
        if (tpm.hasTile()) {
            return new GuiParameters(OMLib.instance, 0, player.getEntityWorld(),
                                     tpm.getTile().getPos().getX(),
                                     tpm.getTile().getPos().getY(),
                                     tpm.getTile().getPos().getZ());
        } else {
            return null;
        }
    }
}