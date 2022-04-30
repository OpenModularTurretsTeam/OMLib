package omtteam.omlib.blocks;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import omtteam.omlib.api.network.INetworkCable;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.tileentity.TileEntityCable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
public class BlockCable extends BlockAbstractTileEntity {
    private static final PropertyBool UP = PropertyBool.create("up");
    private static final PropertyBool DOWN = PropertyBool.create("down");
    private static final PropertyBool NORTH = PropertyBool.create("north");
    private static final PropertyBool EAST = PropertyBool.create("east");
    private static final PropertyBool SOUTH = PropertyBool.create("south");
    private static final PropertyBool WEST = PropertyBool.create("west");

    public BlockCable() {
        super(Material.IRON);
        this.setUnlocalizedName(OMLibNames.Blocks.networkCable);
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Blocks.networkCable);
        this.setHardness(2F);
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public TileEntity createTileEntity_OM(World world, IBlockState state) {
        return new TileEntityCable();
    }

    @Override
    @ParametersAreNonnullByDefault
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof INetworkCable) {
            INetworkCable cable = (INetworkCable) entity;
            return this.getDefaultState()
                    .withProperty(UP, cable.shouldConnect(EnumFacing.UP))
                    .withProperty(DOWN, cable.shouldConnect(EnumFacing.DOWN))
                    .withProperty(NORTH, cable.shouldConnect(EnumFacing.NORTH))
                    .withProperty(EAST, cable.shouldConnect(EnumFacing.EAST))
                    .withProperty(SOUTH, cable.shouldConnect(EnumFacing.SOUTH))
                    .withProperty(WEST, cable.shouldConnect(EnumFacing.WEST));
        }
        return this.getDefaultState();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    @ParametersAreNonnullByDefault
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof INetworkCable) {
            ((INetworkCable) entity).scan(worldIn, pos, "");
        }
    }
}
