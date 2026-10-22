package omtteam.omlib.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import omtteam.omlib.OMLib;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.container.ContainerCamo;

public class OMGuiHandler implements IGuiHandler {
    /** GUI ids of OMLib (opened with {@code OMLib.instance}). */
    public static final int TRUSTED_PLAYERS_GUI = 0;
    public static final int TARGET_SETTINGS_GUI = 1;
    /** The camouflage menu; open it on the server ({@code MessageOpenCamoMenu.open}), it has a container. */
    public static final int CAMO_GUI = 2;

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
            case CAMO_GUI:
                return tileEntity instanceof ICamoSupport
                        ? new ContainerCamo(player.inventory, (ICamoSupport) tileEntity) : null;
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return OMLib.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
