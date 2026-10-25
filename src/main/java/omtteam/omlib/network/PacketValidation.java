package omtteam.omlib.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import omtteam.omlib.reference.OMLibNames;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side checks shared by all C2S messages of the OM mods. Never trust the client: every handler finds its block
 * through {@link #find} (loaded, within reach) and then checks the player's access level for its action.
 */
public final class PacketValidation {
    /** Squared reach of the OM containers ({@code Container.canInteractWith}). */
    public static final double MAX_DISTANCE_SQ = 64.0;

    private PacketValidation() {
    }

    /**
     * The tile entity at {@code pos} if it is loaded (looking it up must never load or generate a chunk), the player is
     * within reach and it is a {@code type}.
     */
    public static <T> Optional<T> find(EntityPlayerMP player, BlockPos pos, Class<T> type) {
        World world = player.getEntityWorld();
        if (!world.isBlockLoaded(pos) || player.getDistanceSqToCenter(pos) > MAX_DISTANCE_SQ) {
            return Optional.empty();
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        return type.isInstance(tileEntity) ? Optional.of(type.cast(tileEntity)) : Optional.empty();
    }

    /** Runs {@code task} with the sending player on the server thread (message handlers run on a network thread). */
    public static void schedule(MessageContext ctx, Consumer<EntityPlayerMP> task) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> task.accept(player));
    }

    /** Tells the player that they lack the access level for an action. */
    public static void sendNoPermission(EntityPlayerMP player) {
        player.sendMessage(new TextComponentTranslation(OMLibNames.Localizations.Text.STATUS_PERMISSION));
    }
}
