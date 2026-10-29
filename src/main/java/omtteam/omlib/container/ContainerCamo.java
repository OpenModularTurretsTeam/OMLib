package omtteam.omlib.container;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import omtteam.omlib.api.render.camo.ICamoSupport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Camouflage menu of camouflage hosts (OMT turret bases, OMPD traps; 1.20.1+ {@code CamoMenu}): one phantom slot for
 * the disguise, the player inventory to pick blocks from and, for hosts with adjustable light, the light sliders of
 * the GUI.
 * <p>
 * The phantom slot never holds an item: clicking it with a block in the cursor sets that block as the disguise (the
 * block stays in the cursor), clicking it with an empty cursor resets the disguise, and shift-clicking a block in the
 * inventory sets it as well. The server checks the permission on every change.
 */
public class ContainerCamo extends Container {
    /** Index of the phantom slot (after the 36 inventory slots). */
    public static final int PHANTOM_SLOT = 36;
    public static final int PHANTOM_X = 80;
    public static final int PHANTOM_Y = 22;
    private static final int INVENTORY_SLOTS = 36;

    private final ICamoSupport host;
    private final boolean showLight;
    private final InventoryBasic display = new InventoryBasic("camo", false, 1);

    public ContainerCamo(InventoryPlayer inventory, ICamoSupport host) {
        this.host = host;
        this.showLight = host.supportsLight();
        int inventoryTop = inventoryTop(showLight);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(inventory, 9 + x + y * 9, 8 + x * 18, inventoryTop + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(inventory, x, 8 + x * 18, inventoryTop + 58));
        }
        display.setInventorySlotContents(0, itemFor(host));
        addSlotToContainer(new Slot(display, 0, PHANTOM_X, PHANTOM_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer player) {
                return false;
            }
        });
    }

    /** The item shown in the disguise slot; empty while the host shows its own look. */
    public static ItemStack itemFor(ICamoSupport host) {
        IBlockState camo = host.getCamoSettings().getCamoBlockState();
        if (camo == null || camo.getBlock() == host.getDefaultCamoState().getBlock()) {
            return ItemStack.EMPTY;
        }
        Block block = camo.getBlock();
        return new ItemStack(block, 1, block.damageDropped(camo));
    }

    /** Top of the inventory slots; lower when the light sliders are shown. */
    public static int inventoryTop(boolean showLight) {
        return showLight ? 106 : 62;
    }

    public static int guiHeight(boolean showLight) {
        return inventoryTop(showLight) + 82;
    }

    public ICamoSupport getHost() {
        return host;
    }

    public boolean showsLight() {
        return showLight;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotId == PHANTOM_SLOT) {
            if (player instanceof EntityPlayerMP && clickType == ClickType.PICKUP) {
                setDisguiseFrom((EntityPlayerMP) player, player.inventory.getItemStack());
            }
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    /** Shift-click on an inventory slot sets the disguise (the stack stays where it is). */
    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index >= 0 && index < INVENTORY_SLOTS && player instanceof EntityPlayerMP) {
            setDisguiseFrom((EntityPlayerMP) player, inventorySlots.get(index).getStack());
        }
        return ItemStack.EMPTY;
    }

    /** An empty stack resets the disguise, a block sets it; anything else is ignored. */
    private void setDisguiseFrom(EntityPlayerMP player, ItemStack stack) {
        if (host.getCamoTileEntity().isInvalid() || !host.isCamoEnabled()) {
            return;
        }
        if (!host.canSetCamo(player)) {
            player.sendMessage(new TextComponentTranslation("text.omlib.camo_not_allowed"));
            return;
        }
        IBlockState disguise = stack.isEmpty() ? host.getDefaultCamoState() : host.getDisguiseRules().disguiseFor(stack);
        if (disguise == null) {
            player.sendMessage(host.getDisguiseRules().invalidMessage());
            return;
        }
        host.setCamoState(disguise);
        host.getCamoTileEntity().markDirty();
        display.setInventorySlotContents(0, itemFor(host));
        // vanilla sends no slot updates while it handles a click packet and then counts the stack as sent, so the
        // change of this phantom slot has to be sent by hand
        player.connection.sendPacket(new SPacketSetSlot(windowId, PHANTOM_SLOT, display.getStackInSlot(0)));
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canInteractWith(EntityPlayer player) {
        TileEntity tileEntity = host.getCamoTileEntity();
        if (player.getEntityWorld().isRemote) {
            return true;
        }
        return !tileEntity.isInvalid() && player.getDistanceSqToCenter(tileEntity.getPos()) < 64
                && host.isCamoEnabled() && (host.canSetCamo(player) || host.canChangeLight(player));
    }
}
