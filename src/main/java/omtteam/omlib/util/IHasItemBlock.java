package omtteam.omlib.util;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

/**
 * Created by Keridos on 07/12/16.
 * This Class
 */
public interface IHasItemBlock {
    ItemBlock getItemBlock(Block block);
}
