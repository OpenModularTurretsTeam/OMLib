package omtteam.omlib.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import omtteam.omlib.api.permission.IHasOwner;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by nico on 23/05/17.
 */
public class ItemMultiTool extends Item {
    public ItemMultiTool() {
        super();
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Items.multiTool);
        this.setUnlocalizedName(OMLibNames.Items.multiTool);
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null) {
                if (tileEntity instanceof IHasOwner && playerIn.isSneaking()) {
                    IHasOwner te = (IHasOwner) tileEntity;
                    if (PlayerUtil.isPlayerAdmin(playerIn, te)) {
                        worldIn.destroyBlock(tileEntity.getPos(), true);
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }
}
