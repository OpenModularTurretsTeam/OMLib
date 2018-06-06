package omtteam.omlib.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.init.OMLibItems;

@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused"})
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    public static World getWorld(Minecraft mc) {
        return mc.world;
    }

    @SuppressWarnings("SameParameterValue")
    private void registerItemModel(final Item item, int meta) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName().toString().toLowerCase()));
    }

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void initRenderers() {
        super.initRenderers();
        registerItemModel(OMLibItems.debugTool, 0);
    }

    @Override
    public void initHandlers() {
        super.initHandlers();
    }

    @Override
    public void init() {
        super.init();
    }
}