package omtteam.omlib.util.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;
import omtteam.omlib.util.GeneralUtil;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;
import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;

/**
 * Created by nico on 6/4/15.
 * Command for changing owners of an owned block.
 */

public class CommandChangeOwner extends CommandBase {
    @Override
    @Nonnull
    public String getName() {
        return "omchangeowner";
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public String getUsage(ICommandSender sender) {
        return "<dimension> <x> <y> <z> <new owner>";
    }

    @Override
    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSender sender, String[] params) {
        if (params.length != 5) {
            addChatMessage(sender, new TextComponentString(getUsage(sender)));
            return;
        }
        try {
            int dimension = Integer.parseInt(params[0]);
            int x = Integer.parseInt(params[1]);
            int y = Integer.parseInt(params[2]);
            int z = Integer.parseInt(params[3]);
            String ownerName = params[4];
            if (DimensionManager.getWorld(dimension) == null) {
                addChatMessage(sender, new TextComponentString("Invalid dimension"));
                return;
            }
            WorldServer worldserver = server.getWorld(dimension);

            TileEntity tileEntity = worldserver.getTileEntity(new BlockPos(x, y, z));
            if (tileEntity instanceof TileEntityOwnedBlock) {
                TileEntityOwnedBlock block = (TileEntityOwnedBlock) tileEntity;
                UUID uuid = getPlayerUUID(ownerName);
                if (uuid != null) {
                    block.setOwner(new Player(uuid, ownerName));
                    addChatMessage(sender, new TextComponentString("Block ownership has been updated"));
                } else if (!GeneralUtil.isServerInOnlineMode()) {
                    block.setOwner(new Player(null, ownerName));
                    addChatMessage(sender, new TextComponentString("Block ownership has been updated"));
                } else {
                    addChatMessage(sender, new TextComponentString("New owner not valid."));
                }
            } else {
                addChatMessage(sender, new TextComponentString("No ownable block was found at that location"));
            }
        } catch (NumberFormatException e) {
            addChatMessage(sender, new TextComponentString("Dimension and coordinates must be numbers"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}

