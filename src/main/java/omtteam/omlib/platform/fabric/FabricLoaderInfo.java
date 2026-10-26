package omtteam.omlib.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;
import omtteam.omlib.platform.LoaderInfo;

import java.nio.file.Path;

public final class FabricLoaderInfo implements LoaderInfo {
    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
