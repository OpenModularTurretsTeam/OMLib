package omtteam.omlib.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

/**
 * Created by Keridos on 07/12/16.
 * This Class is for all blocks that have a corresponding itemblock
 */
@SuppressWarnings("WeakerAccess")
public interface IHasItemBlock {
    /**
     * This does give back the ItemBlock that the block is linked to
     *
     * @return the ItemBlock instance of the given block.
     */
    ItemBlock getItemBlock(Block block);
}
