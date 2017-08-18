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

@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused"})
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @SuppressWarnings("SameParameterValue")
    private void registerItemModel(final Item item, int meta, final String customName, boolean useCustomName) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(Reference.MOD_ID.toLowerCase() + ":" + customName.toLowerCase()));
    }

    @Override
    public void preInit() {
        super.preInit();

        registerItemModel(OMLibItems.debugTool, 0, OMLibItems.debugTool.getRegistryName().toString(), true);
    }

    @Override
    public void initRenderers() {
        super.initRenderers();
    }

    @Override
    public void initHandlers() {
        super.initHandlers();
    }

    public static World getWorld(Minecraft mc) {
        return mc.world;
    }
}