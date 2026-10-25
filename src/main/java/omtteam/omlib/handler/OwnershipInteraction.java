package omtteam.omlib.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import omtteam.omlib.api.permission.IOwnedHost;
import omtteam.omlib.config.OMConfig;
import omtteam.omlib.item.ItemOwnershipWand;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Right-clicks on owned blocks of all OpenModular mods (1.20.1+ {@code InteractiveBlock.handleUse} and
 * {@code WandInteraction}), before the block and the held item get the click: the ownership wand in hand first, then
 * claiming a block without owner. Runs on both sides; a handled click is cancelled with {@code SUCCESS} (the client
 * still sends it, so the server decides).
 */
public class OwnershipInteraction {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.isSpectator()) return;
        if (useWand(player, event.getWorld(), event.getHand(), event.getPos()) || claim(player, event.getWorld(),
                event.getHand(), event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    /**
     * A main-hand right-click without sneaking on an owned block without owner (on a turret: its base) claims it and
     * the unowned blocks connected to it ({@link ClaimSpread}); the click does nothing else. Fake players only claim
     * if {@code fakePlayersCanClaim} is set.
     */
    private static boolean claim(EntityPlayer player, World world, EnumHand hand, BlockPos pos) {
        if (hand != EnumHand.MAIN_HAND || player.isSneaking()) return false;
        BlockPos target = ClaimSpread.ownerPos(world, pos);
        IOwnedHost host = ClaimSpread.ownedHost(world, pos);
        if (host == null || host.getOwnerOrNull() != null) return false;
        if (player instanceof FakePlayer && !OMConfig.GENERAL.fakePlayersCanClaim) return false;
        if (!world.isRemote) {
            ClaimSpread.Result result = ClaimSpread.claim(world, target, new Player(player));
            String key = result.limitReached() ? "text.omlib.claimed_limit" : "text.omlib.claimed";
            player.sendStatusMessage(new TextComponentTranslation(key, result.claimed()), true);
        }
        return true;
    }

    /**
     * What the {@link ItemOwnershipWand} does to an owned block (on a turret: its base). Right-click makes the player
     * the owner with an empty trust list, sneak-right-click removes the owner (and the trust list). With spreading on,
     * the connected blocks with the same owner as the clicked one change too (up to {@code claimSpreadLimit}); other
     * players' blocks next to them stay as they are. Only game masters may use it.
     */
    private static boolean useWand(EntityPlayer player, World world, EnumHand hand, BlockPos pos) {
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemOwnershipWand)) return false;
        BlockPos target = ClaimSpread.ownerPos(world, pos);
        IOwnedHost clicked = ClaimSpread.ownedHost(world, pos);
        if (clicked == null) return false;
        if (!world.isRemote) {
            if (ItemOwnershipWand.mayUse(player)) {
                applyWand(player, world, target, clicked, player.isSneaking(), ItemOwnershipWand.isSpreading(stack));
            } else {
                player.sendStatusMessage(new TextComponentTranslation("text.omlib.wand.game_masters_only"), true);
            }
        }
        return true;
    }

    private static void applyWand(EntityPlayer player, World world, BlockPos target, IOwnedHost clicked,
                                  boolean reset, boolean spread) {
        UUID owner = ownerUuid(clicked);
        List<IOwnedHost> hosts = spread
                ? ClaimSpread.connected(world, target, OMConfig.GENERAL.claimSpreadLimit,
                host -> sameOwner(ownerUuid(host), owner))
                : Collections.singletonList(clicked);
        Player identity = new Player(player);
        for (IOwnedHost host : hosts) {
            // clearing first also empties the trust list of a block that gets a new owner
            host.clearOwner();
            if (!reset) host.claim(identity);
        }
        String key = reset ? "text.omlib.wand.reset" : "text.omlib.wand.claimed";
        player.sendStatusMessage(new TextComponentTranslation(key, hosts.size()), true);
    }

    @Nullable
    private static UUID ownerUuid(IOwnedHost host) {
        Player owner = host.getOwnerOrNull();
        return owner == null ? null : owner.getUuid();
    }

    private static boolean sameOwner(@Nullable UUID a, @Nullable UUID b) {
        return a == null ? b == null : a.equals(b);
    }
}
