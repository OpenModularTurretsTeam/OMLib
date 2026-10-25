package omtteam.omlib.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.permission.IOwnedHost;
import omtteam.omlib.handler.ClaimSpread;

import java.util.Random;

/**
 * Shows that an owned block has no owner, so players see what they can claim (1.20.1+ {@code UnownedMarker}): a green
 * sparkle above it, called from the blocks' {@code randomDisplayTick} (client only; vanilla calls it for random blocks
 * near the player, so a single particle per call already gives a calm rate). Not used by the camouflage trap, which
 * would give itself away.
 */
@SideOnly(Side.CLIENT)
public final class UnownedMarker {
    private UnownedMarker() {
    }

    /** A sparkle above {@code pos} if the block there (or, for an addon such as a turret, its base) has no owner. */
    public static void randomDisplayTick(World world, BlockPos pos, Random random) {
        TileEntity tile = world.getTileEntity(ClaimSpread.ownerPos(world, pos));
        if (!(tile instanceof IOwnedHost) || ((IOwnedHost) tile).getOwnerOrNull() != null) return;
        world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, pos.getX() + random.nextDouble(),
                pos.getY() + 1.05D + random.nextDouble() * 0.3D, pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
    }
}
