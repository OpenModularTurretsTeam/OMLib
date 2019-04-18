package omtteam.omlib.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.client.gui.TrustedPlayersGUI;

public class OMGuiHandler implements IGuiHandler {
    private static OMGuiHandler instance;

    private OMGuiHandler() {
    }

    public static OMGuiHandler getInstance() {
        if (instance == null) {
            instance = new OMGuiHandler();
        }
        return instance;
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        switch (id) {
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null) {
            return null;
        }

        switch (id) {
            case 0:
                return new TrustedPlayersGUI(player.inventory, (IHasTrustManager) tileEntity);
            default:
                return null;
        }
    }
}