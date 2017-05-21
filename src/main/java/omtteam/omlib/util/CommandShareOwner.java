package omtteam.omlib.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import omtteam.omlib.compatability.minecraft.CompatCommandBase;
import omtteam.omlib.handler.OwnerShareHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.handler.ConfigHandler.offlineModeSupport;
import static omtteam.omlib.util.PlayerUtil.getPlayerUUID;
import static omtteam.omlib.util.compat.ChatTools.addChatMessage;

/**
 * Created by nico on 6/4/15.
 * Command for changing owners of an owned block
 */

public class CommandShareOwner extends CompatCommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "omtshareowner";
    }


    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public String getUsage(ICommandSender sender) {
        return "<add|del> <name to share>";
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender sender, String[] params) {
        if (params.length != 2) {
            addChatMessage((EntityPlayer) sender, new TextComponentString(getUsage(sender)));
            return;
        }
        try {
            String shareName = params[1];
            String command = params[0];
            if (command.equals("add")){
                if (!offlineModeSupport && getPlayerUUID(shareName) != null){
                    Player sharePlayer = new Player(getPlayerUUID(shareName), shareName);
                    Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                    OwnerShareHandler.getInstance().addSharePlayer(owner, sharePlayer);
                }
            }
            if (command.equals("add")){
                if (!offlineModeSupport && getPlayerUUID(shareName) != null){
                    Player sharePlayer = new Player(getPlayerUUID(shareName), shareName);
                    Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                    OwnerShareHandler.getInstance().removeSharePlayer(owner, sharePlayer);
                }
            }

        } catch (NumberFormatException e) {
            addChatMessage((EntityPlayer) sender, new TextComponentString("Please supply a valid name"));
        }
    }
}

