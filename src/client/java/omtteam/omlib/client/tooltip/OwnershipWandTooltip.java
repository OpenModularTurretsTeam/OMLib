package omtteam.omlib.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import omtteam.omlib.item.OwnershipWandItem;

import java.util.List;

/** Ownership wand tooltip: its usage and whether it spreads to connected blocks. A mod can add lines of its own. */
public final class OwnershipWandTooltip {
    private static final String TOOLTIP = "tooltip.omlib.ownership_wand.";

    private OwnershipWandTooltip() {
    }

    @SuppressWarnings("unused")
    public static void append(ItemStack stack, TooltipFlag flag, List<Component> lines) {
        if (!(stack.getItem() instanceof OwnershipWandItem)) return;
        for (String usage : List.of("desc1", "desc2", "desc3")) {
            lines.add(Component.translatable(TOOLTIP + usage).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable(TOOLTIP + (OwnershipWandItem.isSpreading(stack) ? "spread_on" : "spread_off"))
                .withStyle(ChatFormatting.AQUA));
    }
}
