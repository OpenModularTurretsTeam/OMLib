package omtteam.omlib.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import omtteam.omlib.api.IDebugTile;
import omtteam.omlib.compatability.minecraft.CompatItem;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.tileentity.TileEntityElectric;
import omtteam.omlib.tileentity.TileEntityMachine;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

/**
 * Created by nico on 23/05/17.
 */
public class ItemDebugTool extends CompatItem {
    public ItemDebugTool() {
        super();

        this.setHasSubtypes(true);
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Items.debugTool);
        this.setUnlocalizedName(OMLibNames.Items.debugTool);
    }

    @Override
    protected EnumActionResult clOnItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null) {
                if (tileEntity instanceof TileEntityOwnedBlock) {
                    TileEntityOwnedBlock te = (TileEntityOwnedBlock) tileEntity;
                    playerIn.sendMessage(new TextComponentString("Owner name: " + te.getOwnerName() + " and UUID is" + te.getOwner()));
                }
                if (tileEntity instanceof TileEntityElectric) {
                    TileEntityElectric te = (TileEntityElectric) tileEntity;
                    playerIn.sendMessage(new TextComponentString("Stored Energy:" + te.getEnergyLevel(EnumFacing.DOWN) + "/" + te.getMaxEnergyLevel(EnumFacing.DOWN)));
                    playerIn.sendMessage(new TextComponentString("Stored EU:" + te.getStorageEU()));
                }
                if (tileEntity instanceof TileEntityMachine) {
                    TileEntityMachine te = (TileEntityMachine) tileEntity;
                    playerIn.sendMessage(new TextComponentString("Active:" + te.isActive() + ", Inverted: " + te.getInverted() + ", Redstone: " + te.getRedstone()));
                }
                if (tileEntity instanceof IDebugTile) {
                    String[] debugInfo = ((IDebugTile) tileEntity).getDebugInfo();
                    if (debugInfo.length >= 1) {
                        for (String debug : debugInfo) {
                            playerIn.sendMessage(new TextComponentString(debug));
                        }
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }
}
