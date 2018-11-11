package omtteam.omlib.blocks;

import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 05/12/2015.
 * This Class
 */
@SuppressWarnings("unused")
public abstract class BlockAbstractMiscPane extends BlockPane {
    protected BlockAbstractMiscPane(String par1, String par2, Material material, boolean par4) {
        super(material, par4);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public abstract TileEntity createTileEntity(World world, IBlockState state);

    @Override
    @ParametersAreNonnullByDefault
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }
}
