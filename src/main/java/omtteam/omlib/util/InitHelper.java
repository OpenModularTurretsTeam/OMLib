package omtteam.omlib.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import omtteam.omlib.api.block.IHasItemBlock;

import java.util.List;

/**
 * Created by Keridos on 07/12/16.
 * This Class
 */
@SuppressWarnings("unused")
public class InitHelper {
    public static Block registerBlock(Block block, IForgeRegistry<Block> registry, List<Item> subblocks) {
        registry.register(block);
        if (block instanceof IHasItemBlock) {
            subblocks.add(((IHasItemBlock) block).getItemBlock(block));
        } else if (block.getRegistryName() != null) {
            subblocks.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
        return block;
    }

    public static Item registerItem(Item item, IForgeRegistry<Item> registry) {
        registry.register(item);
        return item;
    }

    public static SoundEvent registerSound(SoundEvent soundEvent, IForgeRegistry<SoundEvent> registry) {
        registry.register(soundEvent);
        return soundEvent;
    }

    public static <T extends TileEntity> void registerTileEntity(Class<T> tileEntity, String modid, String name) {
        GameRegistry.registerTileEntity(tileEntity, new ResourceLocation(modid, name));
    }
}
