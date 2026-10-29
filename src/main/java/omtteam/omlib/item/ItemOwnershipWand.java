package omtteam.omlib.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

/**
 * The map maker's ownership wand (1.20.1+ {@code OwnershipWandItem}, creative only): right-click an owned block to
 * become its owner, sneak-right-click to remove its owner (see {@link omtteam.omlib.handler.OwnershipInteraction});
 * sneak-right-click into the air switches whether this spreads to the connected blocks of the same owner (on by
 * default). Only game masters (operators in creative mode) can use it. While held, turret shots do not affect the
 * player ({@link #isHeldBy}).
 */
public class ItemOwnershipWand extends Item {
    private static final String NO_SPREAD = "OwnershipWandNoSpread";
    private static final String PREFIX = "tooltip.omlib.ownership_wand.";

    public ItemOwnershipWand() {
        super();
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Items.ownershipWand);
        this.setTranslationKey(OMLibNames.Items.ownershipWand);
        this.setMaxStackSize(1);
        // Open Modular Turrets moves it into its own tab when it is installed (ModItems.init), as on 1.20.1+
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    /** Epic (purple name), as on 1.20.1+: a map maker's tool. */
    @Nonnull
    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    /** Whether the wand changes the connected blocks of the same owner too (the default), or only the clicked one. */
    public static boolean isSpreading(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null || !tag.getBoolean(NO_SPREAD);
    }

    public static void setSpreading(ItemStack stack, boolean spreading) {
        if (spreading) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                tag.removeTag(NO_SPREAD);
                if (tag.isEmpty()) stack.setTagCompound(null);
            }
        } else {
            if (stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setBoolean(NO_SPREAD, true);
        }
    }

    /** Operators in creative mode, like command blocks. */
    public static boolean mayUse(EntityPlayer player) {
        return player.canUseCommandBlock();
    }

    /** A player holding the wand in either hand (turrets may fire at them, but their shots have no effect). */
    public static boolean isHeldBy(Entity entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;
        return player.getHeldItemMainhand().getItem() instanceof ItemOwnershipWand
                || player.getHeldItemOffhand().getItem() instanceof ItemOwnershipWand;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    /** Sneak-right-click into the air switches spreading. */
    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking() || rayTrace(world, player, false) != null) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote) {
            if (mayUse(player)) {
                boolean spreading = !isSpreading(stack);
                setSpreading(stack, spreading);
                player.sendStatusMessage(new TextComponentTranslation("text.omlib.wand."
                        + (spreading ? "spread_on" : "spread_off")), true);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("text.omlib.wand.game_masters_only"), true);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @ParametersAreNonnullByDefault
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        for (int line = 1; line <= 3; line++) {
            tooltip.add(TextFormatting.GRAY + safeLocalize(PREFIX + "desc" + line));
        }
        tooltip.add(TextFormatting.AQUA + safeLocalize(PREFIX + (isSpreading(stack) ? "spread_on" : "spread_off")));
        // the turret immunity last, as on 1.20.1+ (where OMT appends it)
        tooltip.add(TextFormatting.GRAY + safeLocalize(PREFIX + "desc4"));
    }
}
