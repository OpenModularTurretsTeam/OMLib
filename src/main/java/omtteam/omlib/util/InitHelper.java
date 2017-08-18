package omtteam.omlib.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import omtteam.omlib.api.IHasItemBlock;

/**
 * Created by Keridos on 07/12/16.
 * This Class
 */
@SuppressWarnings("unused")
public class InitHelper {
    public static Block registerBlock(Block block) {
        ForgeRegistries.BLOCKS.register(block);
        if (block instanceof IHasItemBlock) {
            ForgeRegistries.ITEMS.register(((IHasItemBlock) block).getItemBlock(block));
        } else {
            ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
        return block;
    }

    public static Item registerItem(Item item) {
        ForgeRegistries.ITEMS.register(item);
        return item;
    }

    public static SoundEvent registerSound(SoundEvent soundEvent) {
        int size = SoundEvent.REGISTRY.getKeys().size();
        SoundEvent.REGISTRY.register(size, soundEvent.getRegistryName(), soundEvent);
        return soundEvent;
    }
}
