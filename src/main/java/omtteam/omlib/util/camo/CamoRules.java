package omtteam.omlib.util.camo;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import omtteam.omlib.api.block.IPlainLookingBlock;

import javax.annotation.Nullable;

/**
 * Which blocks may be used as a disguise, and the block of an item (1.20.1+ {@code CamoRules}); hosts ask their
 * {@code DisguiseRules}, whose default uses these rules. The rules are those of
 * 3.x {@code handleCamoActivation}: a full block that is drawn from a model and has no tile entity, unless the block is
 * an {@link IPlainLookingBlock} (such as OMPD's hardened blocks).
 */
public final class CamoRules {
    private CamoRules() {
    }

    public static boolean isValidDisguise(IBlockState state) {
        return isValidDisguise(state, false);
    }

    /**
     * Feature compatible to 1.20.1+: there a disguise only has to have a full collision shape
     * ({@code isCollisionShapeFullBlock}), so glass, ice and leaves work; here that is the case with
     * {@code allowTransparent}. Without it only opaque full blocks are accepted, as in 3.x. {@code isFullCube} is no
     * test for this, {@code BlockGlass} says it is not one although its collision box is a whole block.
     *
     * @param allowTransparent also accept blocks with a full collision box that are not opaque (glass, ice, leaves),
     *                         as OMLib's default {@code DisguiseRules} do
     */
    @SuppressWarnings("deprecation")
    public static boolean isValidDisguise(IBlockState state, boolean allowTransparent) {
        Block block = state.getBlock();
        boolean shape = block.isFullBlock(state) || allowTransparent && hasFullCollisionBox(state);
        // camouflaged blocks have a tile entity, so they are excluded too
        return shape && state.getMaterial() != Material.AIR
                && (!block.hasTileEntity(state) || block instanceof IPlainLookingBlock)
                && state.getRenderType() == EnumBlockRenderType.MODEL;
    }

    /** The 1.20.1+ {@code isCollisionShapeFullBlock}: the collision box of the state is the whole block. */
    private static boolean hasFullCollisionBox(IBlockState state) {
        try {
            return Block.FULL_BLOCK_AABB.equals(state.getCollisionBoundingBox(EmptyAccess.INSTANCE, BlockPos.ORIGIN));
        } catch (RuntimeException e) {
            // a block whose shape needs a real world is no plain cube
            return false;
        }
    }

    /** The block state a held stack stands for (the item's block with the item's metadata), whether valid or not. */
    @Nullable
    @SuppressWarnings("deprecation")
    public static IBlockState stateFor(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return null;
        }
        ItemBlock item = (ItemBlock) stack.getItem();
        try {
            return item.getBlock().getStateFromMeta(item.getMetadata(stack.getMetadata()));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /** A world of air for asking a block state for its shape (1.20.1+ {@code EmptyBlockGetter}). */
    private static final class EmptyAccess implements IBlockAccess {
        private static final EmptyAccess INSTANCE = new EmptyAccess();

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return null;
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return 0;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return true;
        }

        @Override
        public Biome getBiome(BlockPos pos) {
            return Biomes.PLAINS;
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        @Override
        public WorldType getWorldType() {
            return WorldType.DEFAULT;
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean defaultValue) {
            return defaultValue;
        }
    }
}
