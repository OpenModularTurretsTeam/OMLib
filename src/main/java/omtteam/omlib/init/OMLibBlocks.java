package omtteam.omlib.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import omtteam.omlib.blocks.BlockCable;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.tileentity.TileEntityCable;
import omtteam.omlib.util.InitHelper;

public class OMLibBlocks {
    public static Block networkCable;

    public static void initBlocks(IForgeRegistry<Block> registry) {
        networkCable = new BlockCable();
        InitHelper.registerBlock(networkCable, registry, OMLibItems.subBlocks);
    }

    public static void initTileEntities() {
        GameRegistry.registerTileEntity(TileEntityCable.class, Reference.MOD_ID + ":" + OMLibNames.Blocks.networkCable);
    }
}
