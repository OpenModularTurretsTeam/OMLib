package omtteam.omlib.compat.valkyrien;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.compat.OMLibModCompatibility;

import javax.annotation.Nullable;

/**
 * Finds the {@link ShipFrame} of a block. Without a ship mod (Rifted Valkyrium or Valkyrien Skies 1, mod id
 * {@code valkyrienskies}) every block is in {@link ShipFrame#WORLD}.
 */
public final class ShipFrames {
    private ShipFrames() {
    }

    /** The frame of the block at {@code pos}: its ship's, or {@link ShipFrame#WORLD}. */
    public static ShipFrame at(@Nullable World world, BlockPos pos) {
        if (world == null || !OMLibModCompatibility.ValkyrienSkiesLoaded) return ShipFrame.WORLD;
        return ValkyrienShipFrame.at(world, pos);
    }
}
