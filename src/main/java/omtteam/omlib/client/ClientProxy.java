package omtteam.omlib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.CommonProxy;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.api.tile.IHasTrustManager;
import omtteam.omlib.client.gui.CamoGui;
import omtteam.omlib.client.gui.TargetSettingsGUI;
import omtteam.omlib.client.gui.TrustedPlayersGUI;
import omtteam.omlib.client.network.ClientMessageHandlers;
import omtteam.omlib.handler.OMGuiHandler;
import omtteam.omlib.registry.OMLibItems;

import javax.annotation.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    public static World getWorld(Minecraft mc) {
        return mc.world;
    }

    static void registerItemModels() {
        registerItemModel(OMLibItems.debugTool, 0);
        registerItemModel(OMLibItems.multiTool, 0);
    }

    @SuppressWarnings("SameParameterValue")
    private static void registerItemModel(final Item item, int meta) {
        if (item.getRegistryName() != null) {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName().toString().toLowerCase()));
        }
    }

    @Override
    public void initHandlers() {
        super.initHandlers();
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @Override
    public <M extends IMessage> IMessageHandler<M, IMessage> clientMessageHandler(Class<M> type) {
        return ClientMessageHandlers.get(type);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null) {
            return null;
        }

        switch (id) {
            case 0:
                return new TrustedPlayersGUI(player.inventory, (IHasTrustManager) tileEntity);
            case 1:
                return new TargetSettingsGUI(player.inventory, tileEntity);
            case OMGuiHandler.CAMO_GUI:
                return tileEntity instanceof ICamoSupport ? new CamoGui(player.inventory, (ICamoSupport) tileEntity) : null;
            default:
                return null;
        }
    }
}
