package omtteam.omlib.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;

/**
 * Created by Keridos on 07/12/16.
 * This Class
 */
public class ReflectionInitHelper {
    public static void registerBlocks(Class clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(Block.class)) {
                try {
                    Block block = (Block) field.get(null);
                    if (block != null) {
                        GameRegistry.register(block);
                        if (block instanceof IHasItemBlock) {
                            GameRegistry.register(((IHasItemBlock) block).getItemBlock(block));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerItems(Class clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(Item.class)) {
                try {
                    Item item = (Item) field.get(null);
                    GameRegistry.register(item);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void registerSounds(Class clazz) {
        int size = SoundEvent.REGISTRY.getKeys().size();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == SoundEvent.class) {
                try {
                    SoundEvent soundEvent = (SoundEvent) field.get(null);
                    if (soundEvent != null) {
                        SoundEvent.REGISTRY.register(size, soundEvent.getSoundName(), soundEvent);
                        size++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
