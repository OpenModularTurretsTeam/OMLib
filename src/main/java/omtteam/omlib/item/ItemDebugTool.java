package omtteam.omlib.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import omtteam.omlib.api.tile.IDebugTile;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


/**
 * Created by nico on 23/05/17.
 */
public class ItemDebugTool extends Item {
    public ItemDebugTool() {
        super();
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Items.debugTool);
        this.setTranslationKey(OMLibNames.Items.debugTool);
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null) {
                if (tileEntity instanceof TileEntityOwnedBlock) {
                    TileEntityOwnedBlock te = (TileEntityOwnedBlock) tileEntity;
                    playerIn.sendMessage(new TextComponentString("Owner name is \"" + te.getOwner().getName() + "\" and UUID is " + te.getOwner().getUuid().toString()));
                }
                IEnergyStorage energy = tileEntity.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN);
                if (energy != null) {
                    playerIn.sendMessage(new TextComponentString("Stored Energy: " + energy.getEnergyStored() + "/" + energy.getMaxEnergyStored()));
                }
                if (tileEntity instanceof IDebugTile) {
                    List<String> debugInfo = ((IDebugTile) tileEntity).getDebugInfo();
                    if (!debugInfo.isEmpty()) {
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
