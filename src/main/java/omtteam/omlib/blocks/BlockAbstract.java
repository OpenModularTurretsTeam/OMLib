package omtteam.omlib.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import omtteam.omlib.compatability.minecraft.CompatBlock;

import javax.annotation.ParametersAreNullableByDefault;

/**
 * Created by Keridos on 05/12/2015.
 * This Class
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BlockAbstract extends CompatBlock {
    public BlockAbstract(Material material) {
        super(material);
    }

    @Override
    @ParametersAreNullableByDefault
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }
}
