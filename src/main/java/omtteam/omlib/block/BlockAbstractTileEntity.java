package omtteam.omlib.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import omtteam.omlib.util.inventory.InvUtil;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 05/12/2015.
 * This Class
 */
@SuppressWarnings({"deprecation"})
@MethodsReturnNonnullByDefault
public abstract class BlockAbstractTileEntity extends BlockAbstract {
    protected BlockAbstractTileEntity(Material material) {
        super(material);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createTileEntity_OM(world, state);
    }

    @ParametersAreNonnullByDefault
    public abstract TileEntity createTileEntity_OM(World world, IBlockState state);

    @Override
    @ParametersAreNonnullByDefault
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @Override
    @ParametersAreNonnullByDefault
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    /** Spills the stacks of {@code inventory} into the world at {@code pos} ({@link InvUtil#dropItems}). */
    protected void dropItems(World worldIn, BlockPos pos, IItemHandlerModifiable inventory) {
        InvUtil.dropItems(worldIn, pos, inventory);
    }
}
