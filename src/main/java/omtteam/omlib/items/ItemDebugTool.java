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
import omtteam.omlib.compatibility.minecraft.CompatItem;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.tileentity.TileEntityElectric;
import omtteam.omlib.tileentity.TileEntityMachine;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import java.util.List;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;

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
                    playerIn.sendMessage(new TextComponentString("Owner name is \"" + te.getOwnerName() + "\" and UUID is " + te.getOwner()));
                }
                if (tileEntity instanceof TileEntityElectric) {
                    TileEntityElectric te = (TileEntityElectric) tileEntity;
                    playerIn.sendMessage(new TextComponentString("Stored Energy: " + te.getEnergyLevel(EnumFacing.DOWN) + "/" + te.getMaxEnergyLevel(EnumFacing.DOWN)));
                    playerIn.sendMessage(new TextComponentString("Stored EU: " + te.getStorageEU()));
                }
                if (tileEntity instanceof TileEntityMachine) {
                    TileEntityMachine te = (TileEntityMachine) tileEntity;
                    playerIn.sendMessage(new TextComponentString("Active: " + te.isActive() + ", Redstone: "
                            + te.getRedstone() + ", Mode: " + safeLocalize(TileEntityMachine.getModeAsLocString(te.getMode()))));
                }
                if (tileEntity instanceof IDebugTile) {
                    List<String> debugInfo = ((IDebugTile) tileEntity).getDebugInfo();
                    if (debugInfo.size() >= 1) {
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
