package omtteam.omlib.util.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import omtteam.omlib.handler.OMConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;

/**
 * Created by nico on 6/4/15.
 * Command for toggling debug messages on interaction with certain blocks.
 */

public class CommandToggleDebug extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "omdebug";
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public String getUsage(ICommandSender sender) {
        return "/omdebug <on|off>";
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender sender, String[] params) {
        if (params.length != 1) {
            addChatMessage(sender, new TextComponentString(getUsage(sender)));
            return;
        }
        try {
            String command = params[0];
            if (command.equals("on")) {
                OMConfig.GENERAL.doDebugChat = true;
                addChatMessage(sender, new TextComponentString("Enabled debug chat."));
            } else if (command.equals("off")) {
                OMConfig.GENERAL.doDebugChat = false;
                addChatMessage(sender, new TextComponentString("Disabled debug chat."));
            }
        } catch (NumberFormatException e) {
            addChatMessage(sender, new TextComponentString("Please supply a valid command"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}

