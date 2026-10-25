package omtteam.omlib.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import omtteam.omlib.util.inventory.InvUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

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

    /** Spills the stacks of {@code inventory} into the world at {@code pos}. */
    protected void dropItems(World worldIn, BlockPos pos, IItemHandler inventory) {
        Random rand = new Random();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (item.isEmpty()) {
                continue;
            }
            float rx = rand.nextFloat() * 0.8F + 0.1F;
            float ry = rand.nextFloat() * 0.8F + 0.1F;
            float rz = rand.nextFloat() * 0.8F + 0.1F;

            // a copy keeps everything of the stack (also capability data of other mods)
            EntityItem entityItem = new EntityItem(worldIn, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                                                   item.copy());

            float factor = 0.05F;
            entityItem.motionX = rand.nextGaussian() * factor;
            entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
            entityItem.motionZ = rand.nextGaussian() * factor;
            worldIn.spawnEntity(entityItem);
            InvUtil.setStackSize(item, 0);
        }
    }
}
