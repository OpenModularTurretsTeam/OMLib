package omtteam.omlib.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Created by Keridos on 31/01/17.
 * This Class
 */
public class BlockUtil {
    public static void writeBlockFromStateToNBT(NBTTagCompound nbtTagCompound, IBlockState state) {
        if (state != null && state.getBlock().getRegistryName() != null) {
            nbtTagCompound.setString("camoBlockRegName", state.getBlock().getRegistryName().toString());
            nbtTagCompound.setInteger("camoBlockMeta", state.getBlock().getMetaFromState(state));
        }
    }

    public static IBlockState getBlockStateFromNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasKey("camoBlockRegName") && nbtTagCompound.hasKey("camoBlockMeta")) {
            Block block = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation(nbtTagCompound.getString("camoBlockRegName")));
            if (block != null) {
                return block.getStateFromMeta(nbtTagCompound.getInteger("camoBlockMeta"));
            }
        }
        return null;
    }
}
