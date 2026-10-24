package omtteam.omlib.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.client.config.GuiSlider;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.container.ContainerCamo;
import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.handler.OMGuiHandler;
import omtteam.omlib.network.MessageCamoLight;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.util.player.PlayerUtil;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

/**
 * Camouflage GUI (1.20.1+ {@code CamoScreen}): the disguise slot and, for hosts with adjustable light, the light value
 * and opacity sliders. Opened from another GUI it has a Back button, otherwise (the camouflage trap) its admins get the
 * trusted players button. Drawn without a texture.
 */
public class CamoGui extends GuiContainer {
    private static final int SLOT_SIZE = 18;
    private static final int SLIDER_WIDTH = 160;
    private static final int SLIDER_VALUE_Y = 48;
    private static final int SLIDER_OPACITY_Y = 72;
    private static final int BACK = 0;
    private static final int TRUSTED = 1;

    private final ContainerCamo container;
    private final ICamoSupport host;
    private GuiSlider valueSlider;
    private GuiSlider opacitySlider;

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
        if (GUIBackSystem.getInstance().size() > 0) {
            buttonList.add(new GuiButton(BACK, guiLeft + xSize - 58, guiTop + ContainerCamo.PHANTOM_Y - 1, 50, 20,
                    safeLocalize(OMLibNames.Localizations.GUI.BACK)));
        } else if (host instanceof IHasTrustManager) {
            GuiButton trusted = new GuiButton(TRUSTED, guiLeft + xSize - 72, guiTop + ContainerCamo.PHANTOM_Y - 1, 64,
                    20, safeLocalize(OMLibNames.Localizations.GUI.TRUSTED_PLAYERS));
            trusted.enabled = PlayerUtil.isPlayerAdmin(mc.player, (IHasTrustManager) host);
            buttonList.add(trusted);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BACK) {
            GUIBackSystem.getInstance().openTopGui(mc.player);
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
            drawHoveringText(safeLocalize("tooltip.omlib.camouflage_slot"), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(safeLocalize("text.omlib.camo.disguise") + ":", 8, ContainerCamo.PHANTOM_Y + 5,
                0x404040);
        fontRenderer.drawString(safeLocalize("container.inventory"), 8,
                ContainerCamo.inventoryTop(container.showsLight()) - 11, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int right = guiLeft + xSize;
        int bottom = guiTop + ySize;
        drawRect(guiLeft, guiTop, right, bottom, 0xFF000000);
        drawRect(guiLeft + 1, guiTop + 1, right - 1, bottom - 1, 0xFFC6C6C6);
        drawRect(guiLeft + 1, guiTop + 1, right - 1, guiTop + 2, 0xFFFFFFFF);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + 2, bottom - 1, 0xFFFFFFFF);
        drawRect(right - 2, guiTop + 2, right - 1, bottom - 1, 0xFF555555);
        drawRect(guiLeft + 2, bottom - 2, right - 1, bottom - 1, 0xFF555555);
        for (Slot slot : container.inventorySlots) {
            int x = guiLeft + slot.xPos - 1;
            int y = guiTop + slot.yPos - 1;
            drawRect(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF8B8B8B);
            drawRect(x, y, x + SLOT_SIZE - 1, y + 1, 0xFF373737);
            drawRect(x, y, x + 1, y + SLOT_SIZE - 1, 0xFF373737);
            drawRect(x + 1, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);
            drawRect(x + SLOT_SIZE - 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);
        }
    }
}
