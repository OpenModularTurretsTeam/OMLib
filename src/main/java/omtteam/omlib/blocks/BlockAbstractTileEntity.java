package omtteam.omlib.blocks;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import omtteam.omlib.tileentity.TileEntityContainerElectric;
import omtteam.omlib.util.inventory.InvUtil;

import javax.annotation.Nonnull;
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
    @Nonnull
    @ParametersAreNonnullByDefault
    public abstract TileEntity createTileEntity(World world, IBlockState state);

    @Override
    @ParametersAreNonnullByDefault
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @Override
    @ParametersAreNonnullByDefault
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    protected void dropItems(World worldIn, BlockPos pos) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityContainerElectric) {
            TileEntityContainerElectric entity = (TileEntityContainerElectric) worldIn.getTileEntity(pos);
            Random rand = new Random();
            if (entity != null) {
                for (int i = 0; i < entity.getInventory().getSlots(); i++) {
                    ItemStack item = entity.getInventory().getStackInSlot(i);

                    if (!item.isEmpty()) {
                        float rx = rand.nextFloat() * 0.8F + 0.1F;
                        float ry = rand.nextFloat() * 0.8F + 0.1F;
                        float rz = rand.nextFloat() * 0.8F + 0.1F;

                        EntityItem entityItem = new EntityItem(worldIn, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                                                               new ItemStack(item.getItem(), InvUtil.getStackSize(item),
                                                                             item.getItemDamage()));

                        if (item.hasTagCompound()) {
                            entityItem.getItem().setTagCompound(item.getTagCompound().copy());
                        }

                        float factor = 0.05F;
                        entityItem.motionX = rand.nextGaussian() * factor;
                        entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                        entityItem.motionZ = rand.nextGaussian() * factor;
                        worldIn.spawnEntity(entityItem);
                        InvUtil.setStackSize(item, 0);
                    }
                }
            }
        }
    }
}
