package omtteam.omlib.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Keridos on 05/12/2015.
 * This Class
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BlockAbstract extends Block {
    public BlockAbstract(Material material) {
        super(material);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.getBoundingBox_OM(state, source, pos);
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return this.createBlockState_OM();
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isOpaqueCube(IBlockState state) {
        return this.isOpaqueCube_OM(state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isFullBlock(IBlockState state) {
        return this.isFullBlock_OM(state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isFullCube(IBlockState state) {
        return this.isFullCube_OM(state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return this.canPlaceBlockAt_OM(worldIn, pos);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        this.onBlockPlacedBy_OM(worldIn, pos, state, placer, stack);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return onBlockActivated_OM(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean causesSuffocation(IBlockState state) {
        return this.causesSuffocation_OM(state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        this.onNeighborChange_OM(world, pos, neighbor);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos neighbor) {
        this.neighborChanged_OM(state, worldIn, pos, blockIn, neighbor);
    }

    // ---------------------------------------------------------------------------------------------------
    // Internal Abstraction Functions

    @Nonnull
    protected AxisAlignedBB getBoundingBox_OM(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos);
    }

    @Nonnull
    protected BlockStateContainer createBlockState_OM() {
        return super.createBlockState();
    }

    @ParametersAreNonnullByDefault
    protected boolean isOpaqueCube_OM(IBlockState state) {
        return super.isOpaqueCube(state);
    }

    @ParametersAreNonnullByDefault
    protected boolean isFullBlock_OM(IBlockState state) {
        return super.isFullBlock(state);
    }

    @ParametersAreNonnullByDefault
    protected boolean isFullCube_OM(IBlockState state) {
        return super.isFullCube(state);
    }

    @ParametersAreNonnullByDefault
    protected boolean canPlaceBlockAt_OM(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos);
    }

    @ParametersAreNonnullByDefault
    protected void onBlockPlacedBy_OM(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @ParametersAreNonnullByDefault
    public boolean onBlockActivated_OM(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @ParametersAreNonnullByDefault
    protected boolean causesSuffocation_OM(IBlockState state) {
        return super.causesSuffocation(state);
    }

    @ParametersAreNonnullByDefault
    protected void onNeighborChange_OM(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
    }

    @ParametersAreNonnullByDefault
    protected void neighborChanged_OM(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos neighbor) {
        super.neighborChanged(state, worldIn, pos, blockIn, neighbor);
    }
}
