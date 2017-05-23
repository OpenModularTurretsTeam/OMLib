package omtteam.omlib.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.init.OMLibItems;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.util.compat.WorldTools;

@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused"})
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private void registerItemModel(final Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName().toString().toLowerCase()));
    }

    @SuppressWarnings("SameParameterValue")
    private void registerItemModel(final Item item, int meta, final String customName, boolean useCustomName) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Reference.MOD_ID.toLowerCase() + ":" + customName.toLowerCase()));
    }

    @Override
    public void preInit() {
        super.preInit();

        registerItemModel(OMLibItems.debugTool, 0);
    }

    @Override
    public void initRenderers() {
        super.initRenderers();
    }

    @Override
    public void initHandlers() {
        super.initHandlers();
    }

    @Override
    public void init() {

    }

    public static World getWorld(Minecraft mc) {
        return WorldTools.getWorld(mc);
    }
}