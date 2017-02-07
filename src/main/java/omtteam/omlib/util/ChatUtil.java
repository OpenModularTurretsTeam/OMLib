package omtteam.omlib.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 07/02/17.
 * This Class
 */
public class ChatUtil {

    public static void addChatMessage(@Nonnull EntityPlayer player, @Nonnull ITextComponent component) {
        player.addChatComponentMessage(component);
    }
}
