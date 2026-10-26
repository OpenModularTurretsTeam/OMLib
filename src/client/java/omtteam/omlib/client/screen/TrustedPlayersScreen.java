package omtteam.omlib.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21.11 {
/*import net.minecraft.client.input.KeyEvent;
*///?}
import omtteam.omlib.OMLib;
import omtteam.omlib.client.compat.ClientCompat;
import omtteam.omlib.client.compat.ClientNetworkCompat;
import omtteam.omlib.client.compat.CompatScreen;
import omtteam.omlib.core.security.AccessLevel;
import omtteam.omlib.core.security.TrustedPlayer;
import omtteam.omlib.network.OMLibPackets;
import omtteam.omlib.network.TrustPackets;
import omtteam.omlib.security.PlayerAdapter;
import omtteam.omlib.security.TrustHost;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * Trusted player management of a block with a trust list (layout, texture and buttons of the 1.12.2 OMLib screen). A
 * block that only has the levels "trusted" and "admin" ({@code AccessProfile.TRUSTED_AND_ADMIN}) gets those named and
 * described.
 * <p>
 * Shows the client copy of the trust list (synced with the block entity). Every button only sends a request; the
 * server checks the permission and the change comes back through the block entity sync.
 */
public class TrustedPlayersScreen extends CompatScreen {
    private static final ResourceLocation TEXTURE = OMLib.id("textures/gui/trusted_players.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 256;

    private final BlockPos pos;
    private final Runnable onBack;
    private int left;
    private int top;
    private EditBox nameField;
    private TrustedPlayerList list;

    /**
     * @param onBack what the "Back" button does (OMLib {@code GUIBackSystem}), e.g. returning to the screen this one
     *               was opened from or asking the server to reopen a menu
     */
    public TrustedPlayersScreen(BlockPos pos, Runnable onBack) {
        super(Component.translatable("gui.omlib.trusted_players"));
        this.pos = pos;
        this.onBack = onBack;
    }

    static Component levelName(AccessLevel level) {
        return Component.translatable("gui.omlib.access_level." + level.getSerializedName());
    }

    private Optional<TrustHost> host() {
        if (minecraft == null || minecraft.level == null) return Optional.empty();
        return minecraft.level.getBlockEntity(pos) instanceof TrustHost host ? Optional.of(host) : Optional.empty();
    }

    /** Whether the block only knows "trusted" and "admin". */
    private boolean isSimpleProfile() {
        return host().map(host -> !host.getTrustManager().getProfile().allows(AccessLevel.OPEN_GUI)).orElse(false);
    }

    private Component levelNameForHost(AccessLevel level) {
        return isSimpleProfile() && level == AccessLevel.NONE
                ? Component.translatable("gui.omlib.access_level.trusted") : levelName(level);
    }

    /** Client-side check for the UI only; the server checks again. */
    private boolean isAdmin() {
        return minecraft != null && minecraft.player != null
                && host().map(host -> host.getTrustManager().isAdmin(PlayerAdapter.info(minecraft.player))).orElse(false);
    }

    @Override
    public void removed() {
        ClientCompat.rememberCursor();
        super.removed();
    }

    @Override
    protected void init() {
        ClientCompat.restoreCursor();
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;

        list = addRenderableWidget(new TrustedPlayerList(left + 5, top + 5, 246, 212));
        list.setLevelNamer(this::levelNameForHost);
        nameField = addRenderableWidget(new EditBox(font, left + 11, top + 231, 100, 18,
                Component.translatable("gui.omlib.add_trusted_player")));
        nameField.setMaxLength(TrustPackets.MAX_NAME_LENGTH);
        nameField.setTooltip(tooltip("tp_text_field"));

        addRenderableWidget(Button.builder(Component.literal("+"), button -> addPlayer())
                .bounds(left + 114, top + 230, 33, 20).tooltip(tooltip("add_trusted_player")).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> removeSelected())
                .bounds(left + 148, top + 230, 33, 20).tooltip(tooltip("remove_trusted_player")).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> changeSelected(-1))
                .bounds(left + 185, top + 230, 23, 20).tooltip(tooltip("tp_decrease_access")).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> changeSelected(1))
                .bounds(left + 226, top + 230, 23, 20).tooltip(tooltip("tp_increase_access")).build());
        // positioned to the right of the panel
        addRenderableWidget(Button.builder(Component.translatable("gui.omlib.back"), button -> back())
                .bounds(left + 261, top + 10, 80, 20).build());
        refreshList();
    }

    private static Tooltip tooltip(String key) {
        return Tooltip.create(Component.translatable("tooltip.omlib." + key));
    }

    private void refreshList() {
        host().ifPresent(host -> list.setEntries(host.getTrustManager().getTrustedPlayers()));
    }

    private void back() {
        onBack.run();
    }

    private void addPlayer() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) return;
        ClientNetworkCompat.sendToServer(OMLibPackets.ADD_TRUSTED_PLAYER, new TrustPackets.Add(pos, name));
        nameField.setValue("");
    }

    private void removeSelected() {
        list.getSelected().ifPresent(entry -> ClientNetworkCompat.sendToServer(OMLibPackets.REMOVE_TRUSTED_PLAYER,
                new TrustPackets.Remove(pos, entry.getUuid())));
    }

    private void changeSelected(int delta) {
        list.getSelected().ifPresent(entry -> ClientNetworkCompat.sendToServer(OMLibPackets.CHANGE_ACCESS_LEVEL,
                new TrustPackets.ChangeAccess(pos, entry.getUuid(), delta)));
    }

    /** As in 1.12.2, the screen closes as soon as the player is no longer an admin. */
    @Override
    public void tick() {
        super.tick();
        if (!isAdmin()) {
            onClose();
            return;
        }
        refreshList();
    }

    //? if >=1.21.11 {
    /*@Override
    public boolean keyPressed(KeyEvent event) {
        return addOnEnter(event.key()) || super.keyPressed(event);
    }
    *///?} else {
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return addOnEnter(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?}

    /** Enter in the name field adds the player. */
    private boolean addOnEnter(int keyCode) {
        if (nameField.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            addPlayer();
            return true;
        }
        return false;
    }

    @Override
    protected boolean onScroll(double mouseX, double mouseY, double delta) {
        return list.scroll(mouseX, mouseY, delta);
    }

    @Override
    protected void renderPanel(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ClientCompat.blit(graphics, TEXTURE, left, top, 0, 0, WIDTH, HEIGHT);
        graphics.drawString(font, Component.translatable("gui.omlib.add_trusted_player").append(":"),
                left + 10, top + 220, 0xFF000000, false);
        graphics.drawString(font, Component.translatable("gui.omlib.permissions"),
                left + 186, top + 220, 0xFF000000, false);
    }

    /** Hovering the "Permissions" label explains the selected player's level. */
    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int labelWidth = font.width(Component.translatable("gui.omlib.permissions"));
        boolean overLabel = mouseX >= left + 186 && mouseX < left + 186 + labelWidth
                && mouseY >= top + 218 && mouseY < top + 229;
        if (!overLabel) return;
        Component text = list.getSelected()
                .map(entry -> Component.translatable("tooltip.omlib." + describe(entry, isSimpleProfile())))
                .orElse(Component.translatable("tooltip.omlib.info_access_level"));
        ClientCompat.renderTooltip(graphics, font, text.copy().withStyle(ChatFormatting.WHITE), mouseX, mouseY);
    }

    private static String describe(TrustedPlayer entry, boolean simpleProfile) {
        if (simpleProfile) {
            return entry.getAccessLevel() == AccessLevel.NONE ? "tp_trusted" : "tp_can_manage";
        }
        return switch (entry.getAccessLevel()) {
            case NONE -> "tp_none";
            case OPEN_GUI -> "tp_can_open_gui";
            case CHANGE_SETTINGS -> "tp_can_change_settings";
            case ADMIN -> "tp_can_administer";
        };
    }
}
