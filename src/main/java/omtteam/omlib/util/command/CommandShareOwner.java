package omtteam.omlib.util.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

/**
 * Created by nico on 6/4/15.
 * Command for managing shared owners.
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
        if (params.length > 2 || params.length < 1) {
            addChatMessage(sender, new TextComponentString(getUsage(sender)));
            return;
        }
        try {
            String shareName;
            String command = params[0];
            if (params.length < 2 && command.equalsIgnoreCase("add") || command.equalsIgnoreCase("del")) {
                addChatMessage(sender, new TextComponentString(getUsage(sender)));
                return;
            }
            switch (command) {
                case "add":
                    shareName = params[1];
                    if (!OMConfig.GENERAL.offlineModeSupport && getPlayerUUID(shareName) != null) {
                        Player sharePlayer = new Player(getPlayerUUID(shareName), shareName);
                        Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                        OwnerShareRegister.instance.addSharePlayer(owner, sharePlayer, sender);
                    } else {
                        sender.sendMessage(new TextComponentString("Error while adding " + shareName + " to your Share List!"));
                    }
                    break;
                case "del":
                    shareName = params[1];
                    if (!OMConfig.GENERAL.offlineModeSupport && getPlayerUUID(shareName) != null) {
                        Player sharePlayer = new Player(getPlayerUUID(shareName), shareName);
                        Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                        OwnerShareRegister.instance.removeSharePlayer(owner, sharePlayer, sender);
                    }
                    break;
                case "list":
                    Player owner = new Player(getPlayerUUID(sender.getName()), sender.getName());
                    OwnerShareRegister.instance.printSharePlayers(owner, sender);
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

