package omtteam.omlib.util.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import omtteam.omlib.handler.OwnerShareHandler;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.handler.ConfigHandler.offlineModeSupport;
import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

/**
 * Created by nico on 6/4/15.
 * Command for changing owners of an owned block
 */

public class CommandShareOwner extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "omshareowner";
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public String getUsage(ICommandSender sender) {
        return "'<add|del> <name to share>' or list";
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender sender, String[] params) {
        if (params.length != 2) {
            addChatMessage(sender, new TextComponentString(getUsage(sender)));
            return;
        }
        try {
            String shareName = params[1];
            String command = params[0];
            switch (command) {
                case "add":
                    if (!offlineModeSupport && getPlayerUUID(shareName) != null) {
                        Player sharePlayer = new Player(getPlayerUUID(shareName), shareName);
                        Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                        OwnerShareHandler.getInstance().addSharePlayer(owner, sharePlayer, sender);
                    }
                    break;
                case "del":
                    if (!offlineModeSupport && getPlayerUUID(shareName) != null) {
                        Player sharePlayer = new Player(getPlayerUUID(shareName), shareName);
                        Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                        OwnerShareHandler.getInstance().removeSharePlayer(owner, sharePlayer, sender);
                    }
                    break;
                case "list":
                    Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                    OwnerShareHandler.getInstance().printSharePlayers(owner, sender);
                    break;
            }
        } catch (NumberFormatException e) {
            addChatMessage(sender, new TextComponentString("Please supply a valid name and command"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}

