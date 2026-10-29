package omtteam.omlib.client.gui;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiSlider;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.container.ContainerCamo;
import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.handler.OMGuiHandler;
import omtteam.omlib.network.MessageCamoBack;
import omtteam.omlib.network.MessageCamoLight;
import omtteam.omlib.network.MessageOpenCamoMenu;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nullable;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

/**
 * Camouflage GUI (1.20.1+ {@code CamoScreen}): the disguise slot and, for hosts with adjustable light, the light value
 * and opacity sliders. Opened from another GUI it has a Back button, otherwise (the camouflage trap) its admins get the
 * trusted players button. Back returns to the host's parent menu ({@link ICamoSupport#hasParentMenu()}, opened by the
 * server) or to the GUI on the client's back stack (OMT's configure screen). Drawn without a texture, titled with the
 * block's name.
 */
public class CamoGui extends GuiContainer {
    private static final int SLOT_SIZE = 18;
    /** Dark grey as in the turret base GUI textures, instead of the vanilla light grey. */
    private static final int PANEL = 0xFF585858;
    private static final int PANEL_LIGHT = 0xFF767676;
    private static final int PANEL_SHADOW = 0xFF323232;
    private static final int SLOT = 0xFF464646;
    private static final int SLOT_SHADOW = 0xFF232323;
    private static final int SLOT_LIGHT = 0xFF9A9A9A;
    private static final int LABEL_COLOR = 0x000000;
    private static final int SLIDER_WIDTH = 160;
    private static final int SLIDER_VALUE_Y = 48;
    private static final int SLIDER_OPACITY_Y = 72;
    private static final int BACK = 0;
    private static final int TRUSTED = 1;
    private static final int DISOWN = 4;

    private final ContainerCamo container;
    private final ICamoSupport host;
    private GuiSlider valueSlider;
    private GuiSlider opacitySlider;
    private String title = "";

    public CamoGui(InventoryPlayer inventory, ICamoSupport host) {
        this(new ContainerCamo(inventory, host));
    }

    private CamoGui(ContainerCamo container) {
        super(container);
        this.container = container;
        this.host = container.getHost();
        this.xSize = 176;
        this.ySize = ContainerCamo.guiHeight(container.showsLight());
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        title = blockName();
        if (container.showsLight()) {
            boolean editable = host.canChangeLight(mc.player);
            valueSlider = new GuiSlider(2, guiLeft + 8, guiTop + SLIDER_VALUE_Y, SLIDER_WIDTH, 20,
                    safeLocalize("text.omlib.camo.light_value") + ": ", "", 0, CamoLight.MAX,
                    host.getCamoSettings().getLightValue(), false, true);
            opacitySlider = new GuiSlider(3, guiLeft + 8, guiTop + SLIDER_OPACITY_Y, SLIDER_WIDTH, 20,
                    safeLocalize("text.omlib.camo.light_opacity") + ": ", "", 0, CamoLight.MAX,
                    host.getCamoSettings().getLightOpacity(), false, true);
            valueSlider.enabled = editable;
            opacitySlider.enabled = editable;
            buttonList.add(valueSlider);
            buttonList.add(opacitySlider);
        }
        if (host.hasParentMenu() || GUIBackSystem.getInstance().size() > 0) {
            buttonList.add(new GuiButton(BACK, guiLeft + xSize - 58, guiTop + ContainerCamo.PHANTOM_Y - 1, 50, 20,
                    safeLocalize(OMLibNames.Localizations.GUI.BACK)));
        } else if (host instanceof IHasTrustManager) {
            GuiButton trusted = new GuiButton(TRUSTED, guiLeft + xSize - 8 - 64, guiTop + ContainerCamo.PHANTOM_Y - 1, 64,
                    20, safeLocalize(OMLibNames.Localizations.GUI.TRUSTED));
            trusted.enabled = PlayerUtil.isPlayerAdmin(mc.player, (IHasTrustManager) host);
            buttonList.add(trusted);
            // the camouflage trap's owner; outside the panel, whose row is full
            if (DisownConfirm.isOwner(mc.player, (IHasTrustManager) host)) {
                buttonList.add(DisownConfirm.button(DISOWN, guiLeft + xSize + 4, guiTop + ContainerCamo.PHANTOM_Y - 1,
                        80));
            }
        }
    }

    /** The name of the host's own block (its item, so the tier of a turret base shows), never of its disguise. */
    private String blockName() {
        BlockPos pos = host.getCamoTileEntity().getPos();
        IBlockState state = mc.world.getBlockState(pos);
        ItemStack own = state.getBlock().getItem(mc.world, pos, state);
        return own.isEmpty() ? state.getBlock().getLocalizedName() : own.getDisplayName();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BACK && host.hasParentMenu()) {
            // the parent menu has a container, so the server opens it
            OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageCamoBack(host.getCamoTileEntity().getPos()));
        } else if (button.id == BACK) {
            GUIBackSystem.getInstance().openTopGui(mc.player);
        } else if (button.id == DISOWN) {
            // "No" asks the server for the camouflage menu again
            BlockPos pos = host.getCamoTileEntity().getPos();
            DisownConfirm.ask(pos, () -> OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageOpenCamoMenu(pos)));
        } else if (button.id == TRUSTED) {
            mc.player.openGui(OMLib.instance, OMGuiHandler.TRUSTED_PLAYERS_GUI, mc.world,
                    host.getCamoTileEntity().getPos().getX(), host.getCamoTileEntity().getPos().getY(),
                    host.getCamoTileEntity().getPos().getZ());
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        boolean dragging = valueSlider != null && (valueSlider.dragging || opacitySlider.dragging);
        super.mouseReleased(mouseX, mouseY, state);
        if (dragging) {
            OMLibNetworkingHandler.INSTANCE.sendToServer(new MessageCamoLight(host.getCamoTileEntity().getPos(),
                    valueSlider.getValueInt(), opacitySlider.getValueInt()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        Slot hovered = getSlotUnderMouse();
        if (hovered != null && hovered.slotNumber == ContainerCamo.PHANTOM_SLOT && !hovered.getHasStack()) {
            drawHoveringText(safeLocalize(host.getDisguiseRules().slotHint()), mouseX, mouseY);
        }
        for (GuiButton button : buttonList) {
            String tooltip = button.isMouseOver() ? tooltipKey(button.id) : null;
            if (tooltip != null) {
                drawHoveringText(safeLocalize(tooltip), mouseX, mouseY);
            }
        }
    }

    /** The tooltip of a button, as 1.20.1+ gives the trap's Trusted button and every Disown button one. */
    @Nullable
    private static String tooltipKey(int buttonId) {
        if (buttonId == TRUSTED) {
            return OMLibNames.Localizations.Tooltip.TRUSTED_PLAYER_GUI;
        }
        return buttonId == DISOWN ? "tooltip.omlib.disown" : null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(title, 8, 6, LABEL_COLOR);
        fontRenderer.drawString(safeLocalize("text.omlib.camo.disguise") + ":", 8, ContainerCamo.PHANTOM_Y + 5,
                LABEL_COLOR);
        fontRenderer.drawString(safeLocalize("container.inventory"), 8,
                ContainerCamo.inventoryTop(container.showsLight()) - 11, LABEL_COLOR);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int right = guiLeft + xSize;
        int bottom = guiTop + ySize;
        drawRect(guiLeft, guiTop, right, bottom, 0xFF000000);
        drawRect(guiLeft + 1, guiTop + 1, right - 1, bottom - 1, PANEL);
        drawRect(guiLeft + 1, guiTop + 1, right - 1, guiTop + 2, PANEL_LIGHT);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + 2, bottom - 1, PANEL_LIGHT);
        drawRect(right - 2, guiTop + 2, right - 1, bottom - 1, PANEL_SHADOW);
        drawRect(guiLeft + 2, bottom - 2, right - 1, bottom - 1, PANEL_SHADOW);
        for (Slot slot : container.inventorySlots) {
            int x = guiLeft + slot.xPos - 1;
            int y = guiTop + slot.yPos - 1;
            drawRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, SLOT);
            drawRect(x, y, x + SLOT_SIZE - 1, y + 1, SLOT_SHADOW);
            drawRect(x, y, x + 1, y + SLOT_SIZE - 1, SLOT_SHADOW);
            drawRect(x + 1, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, SLOT_LIGHT);
            drawRect(x + SLOT_SIZE - 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, SLOT_LIGHT);
        }
    }
}
